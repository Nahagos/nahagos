from fastapi import FastAPI, HTTPException, Response, Depends, Cookie
from pydantic import BaseModel
import datetime 
import uuid
from db import Database
import threading
import time
import requests
from lxml import etree

app = FastAPI()
class Station(BaseModel):
    id: int
    name: str
    coords: tuple

class PassengerRequest(BaseModel):
    username: str
    password: str

class DriverLogin(BaseModel):
    id: int
    username: str
    password: str
    
class DriverRegister(BaseModel):
    trip_id : str

class PassengerWait(BaseModel):
    trip_id: str
    stop_id: int

class DriverLocation(BaseModel):
    lon: float
    lat: float
    

users_lock = threading.Lock()
drivers_lock = threading.Lock()
trips_lock = threading.Lock()
db_lock = threading.Lock()

connected_users = {}

connected_drivers = {}      # driver_cookie : driver_id, last_action, trip_id

registered_trips = {}       # trip_id : [[stops_list], None]

db = Database("db.sql")


MOT_API_URL = "http://moran.mot.gov.il:110/Channels/HTTPChannel/SmQuery/2.8/xml?Key=LA353500&MonitoringRef="
PROXY_URL   = "http://gp.lavirz.com:8043/"

class OnlineLineData:
    def __init__(self, scope):
        self.namespaces = {'siri': 'http://www.siri.org.uk/siri'}
        self.scope      = scope

    def get_recorded_at_time(self):
        return self.scope.find(".//siri:RecordedAtTime", namespaces=self.namespaces).text

    def get_item_identifier(self):
        return self.scope.find(".//siri:ItemIdentifier", namespaces=self.namespaces).text

    def get_monitoring_ref(self, monitored_stop_visit):
        return self.scope.find(".//siri:MonitoringRef", namespaces=self.namespaces).text

    def get_line_ref(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:LineRef", namespaces=self.namespaces).text

    def get_direction_ref(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:DirectionRef", namespaces=self.namespaces).text

    def get_data_frame_ref(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:FramedVehicleJourneyRef/siri:DataFrameRef", namespaces=self.namespaces).text

    def get_dated_vehicle_journey_ref(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:FramedVehicleJourneyRef/siri:DatedVehicleJourneyRef", namespaces=self.namespaces).text

    def get_published_line_name(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:PublishedLineName", namespaces=self.namespaces).text

    def get_operator_ref(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:OperatorRef", namespaces=self.namespaces).text

    def get_destination_ref(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:DestinationRef", namespaces=self.namespaces).text

    def get_origin_aimed_departure_time(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:OriginAimedDepartureTime", namespaces=self.namespaces).text

    def get_confidence_level(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:ConfidenceLevel", namespaces=self.namespaces).text

    def get_vehicle_location(self) -> list:
        if vehicle_journey := self.scope.find(".//siri:MonitoredVehicleJourney", namespaces=self.namespaces):
            if vehicle_location := vehicle_journey.find(".//siri:VehicleLocation", namespaces=self.namespaces):
                longitude = vehicle_location.find(".//siri:Longitude", namespaces=self.namespaces).text
                latitude = vehicle_location.find(".//siri:Latitude", namespaces=self.namespaces).text
                return [longitude, latitude]
            
        return None

    def get_bearing(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:Bearing", namespaces=self.namespaces).text

    def get_velocity(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:Velocity", namespaces=self.namespaces).text

    def get_license_plate(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:VehicleRef", namespaces=self.namespaces).text

    def get_stop_point_ref(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:MonitoredCall/siri:StopPointRef", namespaces=self.namespaces).text

    def get_order(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:MonitoredCall/siri:Order", namespaces=self.namespaces).text

    def get_expected_arrival_time(self):
        end_time = datetime.datetime.fromisoformat(self.scope.find(".//siri:MonitoredVehicleJourney/siri:MonitoredCall/siri:ExpectedArrivalTime", namespaces=self.namespaces).text)
        return [end_time.year, end_time.month, end_time.day, end_time.hour, end_time.minute]
    
    def get_distance_from_stop(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:MonitoredCall/siri:DistanceFromStop", namespaces=self.namespaces).text



def driver_connectivity():
    while True:
        drivers_lock.acquire()
        trips_lock.acquire()
        del_drivers = []
        for driver, properties in connected_drivers.items():
            if (datetime.datetime.now() - properties[1]) > datetime.timedelta(minutes=5):
                if properties[2] is not None:
                    del registered_trips[properties[2]]
                del_drivers.append(driver)
        for driver in del_drivers:
            del connected_drivers[driver]
        drivers_lock.release()
        trips_lock.release()
        time.sleep(10)

@app.on_event("startup")
def start_daemon_thread():
    # Create and start the daemon thread
    daemon_thread = threading.Thread(target=driver_connectivity, daemon=True)
    daemon_thread.start()
    print("Daemon thread started!")


@app.get("/")
def root():
    return {"message": "Server Is Legit, V1.0.0"}

@app.get("/get-realtime-lines-mot/{stop_code}")
def get_realtime_lines_mot(stop_code: int, cookies_and_milk :str = Cookie(None)):
    
    proxies = {
        "http": PROXY_URL
    }

    try:
        response = requests.get(MOT_API_URL + str(stop_code), proxies=proxies)
        root = etree.fromstring(response.text.encode("utf-8"))

        if response.status_code == 200:
            namespaces = {'siri': 'http://www.siri.org.uk/siri'}
            data = []
            for monitored_stop_visit in root.xpath(".//siri:MonitoredStopVisit", namespaces=namespaces): # iterating over the scopes
                
                line = OnlineLineData(monitored_stop_visit)
                data.append({line.get_published_line_name(): {"license_plate": line.get_license_plate(),
                                                         "location"     : line.get_vehicle_location(),
                                                         "arrival_time" : line.get_expected_arrival_time(),
                                                         "reliable"     : line.get_confidence_level(),
                                                         "destination"  : line.get_destination_ref(),
                                                         "name"         : line.get_published_line_name(),
                                                         "trip_id"      : line.get_dated_vehicle_journey_ref()
                                                         }})

            return data
            

        else:
            raise HTTPException(status_code=500, detail="Couldn't get data")

    except requests.exceptions.RequestException as e:
        raise HTTPException(status_code=401, detail="Couldn't reach data")

def update_last_active(driver_cookie):
    connected_drivers[driver_cookie][1] = datetime.datetime.now()
 
@app.get("/end-trip/")
def end_trip_by_cookie(cookies_and_milk :str = Cookie(None)):
    """
    The driver informs the server that the trip is over
    """
    drivers_lock.acquire()
    # validate user
    if cookies_and_milk not in connected_drivers:
        drivers_lock.release()
        raise HTTPException(status_code=401, detail="User not authenticated")

    #checks if the driver was on a trip
    if connected_drivers[cookies_and_milk][2] is None:
        drivers_lock.release()
        raise HTTPException(status_code=404, detail="No trip to end")
    trips_lock.acquire()

    del registered_trips[connected_drivers[cookies_and_milk][2]]
    connected_drivers[cookies_and_milk][2] = None
    trips_lock.release()
    drivers_lock.release()
    return {"message": "The trip ended successfully"}

@app.get("/lines-by-station/{stop_id}")
def get_real_time_lines(stop_id: int, cookies_and_milk :str = Cookie(None)):
    """
    Retrives real-time arriving times at given station
    """ 
    users_lock.acquire()
    # validate user
    if cookies_and_milk not in connected_users:
        users_lock.release()
        raise HTTPException(status_code=401, detail="User not authenticated")
    
    db_lock.acquire()
    try:
        list_lines = db.get_lines_by_station(stop_id)
        lines_json = []
        trips = []
        uniqe_lines = []
        for trip in registered_trips.keys():
            trips.append(trip.split("_")[0])
        for line in list_lines:
            real_trip_id = line[0].split("_")[0]
            if real_trip_id not in uniqe_lines:
                uniqe_lines.append(real_trip_id)
                lines_json.append({"trip_id": line[0], "departure": line[1][:4], "name": line[2].replace("<->", "->"), "line_num": line[3], "operator": line[4], "isNahagos" : real_trip_id in trips, "isLive" : False})
        get_realtime(stop_id, lines_json)
        db_lock.release()
        users_lock.release()
        return lines_json
    except Exception as e:
        db_lock.release()
        users_lock.release()
        raise HTTPException(status_code=500, detail=str(e))

def get_realtime(stop_id, line_lst):
    stop_code = db.convert_stop_id_to_code(stop_id)
    if not stop_code:
        return
    data = get_realtime_lines_mot(stop_code)
    for i in data:
        for line_name, values in i.items():
            if 'location' not in values.keys() or values['location'] == 'null':
                continue
            for line in line_lst:
                if line_name == line['line_num'] and not line['isLive']:
                    line['departure'] = f"{str(values['arrival_time'][3]).zfill(2)}:{str(values['arrival_time'][4]).zfill(2)}"
                    line['isLive'] = True
                    break
    print(line_lst[:5])

@app.get("/update-arrival-time/{station_id, bus_id}")
def update_arrival_time(station_id: int, bus_id: int):
    """
    Get real-time bus location and arrival time to a given station
    """

    return {"Arriving": "12:54:32", "Location": {"Lat": "33.4234242", "Lon": "34.334211"}}


@app.post("/passenger/wait-for/")
def passenger_wait_for_bus(wait_for : PassengerWait, cookies_and_milk:str = Cookie(None)):
    """
    Log that a passenger is waiting for a specific bus at a given station.
    """
    users_lock.acquire()
     # Validate user session
    if cookies_and_milk not in connected_users:
        users_lock.release()
        raise HTTPException(status_code=401, detail="User not authenticated")
    
    users_lock.release()
    
    trips_lock.acquire()
    db_lock.acquire()
    if db.check_stop_on_trip(wait_for.trip_id, wait_for.stop_id):
        if wait_for.trip_id in registered_trips.keys():
            registered_trips[wait_for.trip_id][0].add(wait_for.stop_id)
            trips_lock.release()
            db_lock.release()
            return {"message": "Passenger wait request logged successfully"}
        trips_lock.release()
        db_lock.release()
        raise HTTPException(status_code=404, detail="Trip not found")

    trips_lock.release()
    db_lock.release()
    raise HTTPException(status_code=400, detail="Stop is not in this line's route") 



@app.post("/driver/drive/register/")
def register_for_line(reg: DriverRegister, cookies_and_milk: str = Cookie(None)):
    """
    Register a driver for a specific line
    """
    # TODO: change the status of nahagos in this specific line and fix checks for validation of line

    drivers_lock.acquire()
    db_lock.acquire()
    # validate user

    if cookies_and_milk not in connected_drivers:
        drivers_lock.release()
        db_lock.release()
        raise HTTPException(status_code=401, detail="User not authenticated") 
    
    update_last_active(cookies_and_milk)
    
    if not db.check_schedule(reg.trip_id, connected_drivers[cookies_and_milk][0]):
        drivers_lock.release()
        db_lock.release()
        raise HTTPException(status_code=403, detail="Line isn't scheduled for you")
    
    if connected_drivers[cookies_and_milk][2]:
        drivers_lock.release()
        db_lock.release()
        raise HTTPException(status_code=409, detail="You already registered for a trip")
    
    trips_lock.acquire()
    connected_drivers[cookies_and_milk][2] = reg.trip_id
    registered_trips[reg.trip_id] = [set(), None]
    trips_lock.release()
    drivers_lock.release()
    db_lock.release()
    return {"message": "Line registered successfully"}



@app.post("/driver/login/")
def driver_login(driver: DriverLogin, response: Response):
    """
    Check if the id, username and the password are correct
    """
    db_lock.acquire()
    drivers_lock.acquire()
    if db.login_driver(driver.username, driver.password, driver.id):
        session_id = str(uuid.uuid4())  # Generate a unique session ID
        connected_drivers[session_id] = [driver.id, datetime.datetime.now(), None]
        
        response.set_cookie(key="cookies_and_milk", value=session_id, httponly=True)  # Set session ID in a secure cookie
        db_lock.release()
        drivers_lock.release()
        return {"message": "Login successful"}
    else:
        db_lock.release()
        drivers_lock.release()
        raise HTTPException(status_code=403, detail="Invalid credentials")


@app.post("/passenger/login/")
def passenger_login(request: PassengerRequest, response: Response):
    """
    Check if the username and the password are correct
    """
    # Access the username and password from the request
    username = request.username
    password = request.password

    users_lock.acquire()
    db_lock.acquire()
    
    if db.login_passenger(username, password):
        session_id = str(uuid.uuid4())
        connected_users[session_id] = username
        response.set_cookie(key="cookies_and_milk", value=session_id, httponly=True)
        users_lock.release()
        db_lock.release()
        return {"message": "Login successful"}
    else:
        users_lock.release()
        db_lock.release()
        raise HTTPException(status_code=403, detail="Invalid credentials")


@app.post("/passenger/signup/")
def passenger_signup(request: PassengerRequest, response: Response):
    """
    Check if the username and the password can be register, and if so register
    """
    username = request.username
    password = request.password
    
    users_lock.acquire()
    db_lock.acquire()
    
    if db.signup_passenger(username, password):
        session_id = str(uuid.uuid4())  # Generate a unique session ID
        connected_users[session_id] = username
        response.set_cookie(key="cookies_and_milk", value=session_id, httponly=True)  # Set session ID in a secure cookie
        users_lock.release()
        db_lock.release()
        return {"message": "Login successful"}
    else:
        users_lock.release()
        db_lock.release()
        raise HTTPException(status_code=400, detail="Signup failed, username might already exist")


@app.get("/stops-by-line/{trip_id}")
def get_stops_by_line(trip_id : str, cookies_and_milk :str = Cookie(None)):
    """
    Retrives the stops for a specific line
    """ 
    
    users_lock.acquire()
    drivers_lock.acquire()
    # validate user
    if cookies_and_milk not in connected_drivers and cookies_and_milk not in connected_users:
        users_lock.release()
        drivers_lock.release()
        raise HTTPException(status_code=401, detail="User not authenticated") 

    users_lock.release()
    drivers_lock.release()
    db_lock.acquire()
    
    try:
        list_lines = db.get_stops_by_trip_id(trip_id)
        lines_json = []
        for line in list_lines:
            lines_json.append({"stop_id": line[0], "stop_name": line[1], "time": line[2], "stop_lat": line[3], "stop_lon": line[4], 'isNahagos': trip_id in registered_trips.keys()})            
        db_lock.release()
        return lines_json
    except Exception as e:
        db_lock.release()
        raise HTTPException(status_code=500, detail=str(e))

    
@app.post("/driver/where-to-stop/")
def where_to_stop(location: DriverLocation, cookies_and_milk :str = Cookie(None)):
    """
    Retrives the stops that a specific driver need to stop
    """
    drivers_lock.acquire()
    # validate user
    if cookies_and_milk not in connected_drivers:
        drivers_lock.release()
        raise HTTPException(status_code=401, detail="User not authenticated")
    
    trips_lock.acquire()
    if connected_drivers[cookies_and_milk][2] not in registered_trips.keys():
        trips_lock.release()
        drivers_lock.release()
        raise HTTPException(status_code=400, detail="Need to start a trip first")
    update_last_active(cookies_and_milk)
    if connected_drivers[cookies_and_milk][2]:
        registered_trips[connected_drivers[cookies_and_milk][2]][1] = (location.lat, location.lon)
        res = registered_trips[connected_drivers[cookies_and_milk][2]][0]
        trips_lock.release()
        drivers_lock.release()
        return res
    
    trips_lock.release()
    drivers_lock.release()
    return  []


@app.get("/driver/schedule/")
def get_schedule(cookies_and_milk :str = Cookie(None)):
    """
    Retrives the schedule for a specific driver
    """ 
    drivers_lock.acquire()
    # validate user
    if cookies_and_milk not in connected_drivers:
        drivers_lock.release()
        raise HTTPException(status_code=401, detail="User not authenticated") 

    update_last_active(cookies_and_milk)
    try:
        list_lines = db.get_driver_schedule(connected_drivers[cookies_and_milk][0])
        days = ["sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"]
        schedule = [[] for day in days]
        for line in list_lines:
            schedule[days.index(line[0])].append({"trip_id" : line[1], "line_num" : line[2], "departure" : line[3], "name" : line[4]})
        drivers_lock.release()
        return schedule
    except Exception as e:
        drivers_lock.release()
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/line-shape/{trip_id}")
def get_shape(trip_id : str, cookies_and_milk :str = Cookie(None)):
    """
    Retrives the shape for a specific trip
    """ 
    drivers_lock.acquire()
    users_lock.acquire()
    # validate user
    # if cookies_and_milk not in connected_drivers and cookies_and_milk not in connected_users:
    #     drivers_lock.release()
    #     users_lock.release()
    #     raise HTTPException(status_code=401, detail="User not authenticated") 
    drivers_lock.release()
    users_lock.release()
    
    db_lock.acquire()
    
    try:
        list_lines = db.get_trip_shape(trip_id)
        lines_json = []
        for line in list_lines:
            lines_json.append({"latitude" : line[0], "longitude" : line[1]})   
        db_lock.release()         
        return lines_json
    except Exception as e:
        db_lock.release()
        raise HTTPException(status_code=500, detail=str(e))
    
    
    