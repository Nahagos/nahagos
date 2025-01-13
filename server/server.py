from fastapi import FastAPI, HTTPException, Response, Depends, Cookie
from pydantic import BaseModel
from datetime import datetime
import uuid
from db import Database
import threading
import time

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

users_lock = threading.Lock()
drivers_lock = threading.Lock()
trips_lock = threading.Lock()
db_lock = threading.Lock()

connected_users = {}

connected_drivers = {}      # driver_cookie : driver_id, last_action, trip_id

registered_trips = {}       # trip_id : [stops_list]

db = Database("db.sql")

def driver_connectivity():
    while True:
        drivers_lock.acquire()
        trips_lock.acquire()
        for driver, properties in connected_drivers.items():
            if (datetime.now() - properties[1]) > datetime.timedelta(minutes=5):
                del registered_trips[properties[2]]
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


def update_last_active(driver_cookie):
    connected_drivers[driver_cookie][1] = datetime.now()
 
@app.get("/end-trip/")
def end_trip_by_cookie(cookies_and_milk :str = Cookie(None)):
    """
    The driver informs the server that the trip is over
    """
    drivers_lock.acquire()
    # validate user
    if not cookies_and_milk or cookies_and_milk not in connected_drivers:
        drivers_lock.release()
        raise HTTPException(status_code=401, detail="User not authenticated")
    
    #checks if the driver was on a trip
    if connected_drivers[cookies_and_milk] in None:
        drivers_lock.release()
        raise HTTPException(status_code=401, detail="No trip to end")
    
    trips_lock.acquire()
    del registered_trips[connected_drivers[cookies_and_milk]]
    connected_drivers[cookies_and_milk] = None
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
    if not cookies_and_milk or cookies_and_milk not in connected_users:
        users_lock.release()
        raise HTTPException(status_code=401, detail="User not authenticated")
    
    try:
        list_lines = db.get_lines_by_station(stop_id)
        lines_json = []
        for line in list_lines:
            lines_json.append({"trip_id": line[0], "departure": line[1], "name": line[2], "line_num": line[3], "operator": line[4], "isNahagos" : line[0] in registered_trips})            
        users_lock.release()
        return lines_json
    except Exception as e:
        users_lock.release()
        raise HTTPException(status_code=401, detail=str(e))


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
    if not cookies_and_milk or cookies_and_milk not in connected_users:
        users_lock.release()
        raise HTTPException(status_code=401, detail="User not authenticated")
    
    users_lock.release()
    
    trips_lock.acquire()
    db_lock.acquire()
    if db.check_stop_on_trip(wait_for.trip_id, wait_for.stop_id):
        if wait_for.trip_id in registered_trips.keys():
            registered_trips[wait_for.trip_id].add(wait_for.stop_id)
            trips_lock.release()
            db_lock.release()
            return {"message": "Passenger wait request logged successfully"}
        trips_lock.release()
        db_lock.release()
        raise HTTPException(status_code=401, detail="No Nahagos!")

    trips_lock.release()
    db_lock.release()
    raise HTTPException(status_code=401, detail="Stop is not in this lines route") 



@app.post("/driver/drive/register/")
def register_for_line(reg: DriverRegister, cookies_and_milk: str = Cookie(None)):
    """
    Register a driver for a specific line
    """
    # TODO: change the status of nahagos in this specific line and fix checks for validation of line

    drivers_lock.acquire()
    db_lock.acquire()
    # validate user
    if not cookies_and_milk or cookies_and_milk not in connected_drivers:
        drivers_lock.release()
        db_lock.release()
        raise HTTPException(status_code=401, detail="User not authenticated") 
    
    update_last_active(cookies_and_milk)
    
    if not db.check_schedule(reg.trip_id, connected_drivers[cookies_and_milk][0]):
        drivers_lock.release()
        db_lock.release()
        raise HTTPException(status_code=401, detail="Line isn't schedualed for you")
    
    if connected_drivers[cookies_and_milk][2]:
        drivers_lock.release()
        db_lock.release()
        raise HTTPException(status_code=401, detail="You already registered for a trip")
    
    trips_lock.acquire()
    connected_drivers[cookies_and_milk][2] = reg.trip_id
    registered_trips[reg.trip_id] = set()
    trips_lock.release()
    drivers_lock.release()
    db_lock.release()
    return {"message": "Line registered successfully"}


# @app.get("/update-station-list/{last_updated_date}")
# def update_station_list(last_updated_date: str, cookies_and_milk: str = Cookie(None)):
#     """
#     Check whether or not the station list is up to date, and if not sending changes
#     """
#     try:
#         # validate user
#         if not cookies_and_milk or (cookies_and_milk not in connected_drivers and cookies_and_milk not in connected_users):
#             raise HTTPException(status_code=401, detail="User not authenticated") 
        
#         # Validate date format
#         try:
#             last_updated_date = datetime.strptime(last_updated_date, "%Y-%m-%d")
#         except ValueError:
#             raise HTTPException(status_code=400, detail="Invalid date format. Use YYYY-MM-DD.")


#         # Query the database to check for diffs since the last_updated_date
#         diffs = db.get_diffs_since_date(last_updated_date)  # Custom function to fetch diffs

#         if not diffs:  # If no diffs are found, the station list is up-to-date
#             return {"status": "Up to date"}

#         return {"status": "Not up to date", "changes": diffs}

#     except Exception as e:
#         raise HTTPException(status_code=500, detail=f"An error occurred: {str(e)}")

#         if not diffs:  # If no diffs are found, the station list is up-to-date
#             return {"status": "Up to date"}

#         return {"status": "Not up to date", "changes": diffs}

#     except Exception as e:
#         raise HTTPException(status_code=500, detail=f"An error occurred: {str(e)}")

@app.post("/driver/login/")
def driver_login(driver: DriverLogin, response: Response):
    """
    Check if the id, username and the password are correct
    """
    db_lock.acquire()
    drivers_lock.acquire()
    if db.login_driver(driver.username, driver.password, driver.id):
        session_id = str(uuid.uuid4())  # Generate a unique session ID
        connected_drivers[session_id] = [driver.id, None, None]
        
        response.set_cookie(key="cookies_and_milk", value=session_id, httponly=True)  # Set session ID in a secure cookie
        db_lock.release()
        drivers_lock.release()
        return {"message": "Login successful"}
    else:
        db_lock.release()
        drivers_lock.release()
        raise HTTPException(status_code=401, detail="Invalid credentials")


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
        connected_users[session_id] = {"username": username}
        response.set_cookie(key="cookies_and_milk", value=session_id, httponly=True)
        users_lock.release()
        db_lock.release()
        return {"message": "Login successful"}
    else:
        users_lock.release()
        db_lock.release()
        raise HTTPException(status_code=401, detail="Invalid credentials")


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
        connected_users[session_id] = {"username": username}
        response.set_cookie(key="cookies_and_milk", value=session_id, httponly=True)  # Set session ID in a secure cookie
        users_lock.release()
        db_lock.release()
        return {"message": "Login successful"}
    else:
        users_lock.release()
        db_lock.release()
        raise HTTPException(status_code=401, detail="Invalid signup")


@app.get("/stops-by-line/{trip_id}")
def get_stops_by_line(trip_id : str, cookies_and_milk :str = Cookie(None)):
    """
    Retrives the stops for a specific line
    """ 
    
    users_lock.acquire()
    drivers_lock.acquire()
    # validate user
    if not cookies_and_milk or (cookies_and_milk not in connected_drivers and cookies_and_milk not in connected_users):
        users_lock.release()
        drivers_lock.release()
        raise HTTPException(status_code=401, detail="User not authenticated") 

    users_lock.release()
    drivers_lock.release()
    db_lock.acquire()
    
    try:
        list_lines = db.get_stops_by_trip_id(trip_id)
        db_lock.release()
        lines_json = []
        for line in list_lines:
            lines_json.append({"stop_id": line[0], "stop_name": line[1], "time": line[2], "stop_lat": line[3], "stop_lon": line[4]})            
        return lines_json
    except Exception as e:
        db_lock.release()
        raise HTTPException(status_code=401, detail=str(e))

    
@app.get("/driver/where-to-stop/")
def where_to_stop(cookies_and_milk :str = Cookie(None)):
    """
    Retrives the stops that a specific driver need to stop
    """
    drivers_lock.acquire()
    # validate user
    if not cookies_and_milk or cookies_and_milk not in connected_drivers:
        drivers_lock.release()
        raise HTTPException(status_code=401, detail="User not authenticated")
    
    trips_lock.acquire()
    update_last_active(cookies_and_milk)
    if connected_drivers[cookies_and_milk][2]:
        res = registered_trips[connected_drivers[cookies_and_milk][2]]
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
    if not cookies_and_milk or cookies_and_milk not in connected_drivers:
        drivers_lock.release()
        raise HTTPException(status_code=401, detail="User not authenticated") 

    update_last_active(cookies_and_milk)
    try:
        list_lines = db.get_driver_schedule(connected_drivers[cookies_and_milk][0])
        days = {
            "sunday": [],
            "monday": [],
            "tuesday": [],
            "wednesday": [],
            "thursday": [],
            "friday": [],
            "saturday": []
        }
        for line in list_lines:
            days[line[0]].append({"trip_id" : line[1], "line_num" : line[2], "departure" : line[3], "name" : line[4]})
        drivers_lock.release()
        return days
    except Exception as e:
        drivers_lock.release()
        raise HTTPException(status_code=401, detail=str(e))


@app.get("/line-shape/{trip_id}")
def get_shape(trip_id : str, cookies_and_milk :str = Cookie(None)):
    """
    Retrives the shape for a specific trip
    """ 
    drivers_lock.acquire()
    users_lock.acquire()
    # validate user
    if not cookies_and_milk or (cookies_and_milk not in connected_drivers and cookies_and_milk not in connected_users):
        drivers_lock.release()
        users_lock.release()
        raise HTTPException(status_code=401, detail="User not authenticated") 
    drivers_lock.release()
    users_lock.release()
    
    db_lock.acquire()
    
    try:
        list_lines = db.get_trip_shape(trip_id)
        lines_json = []
        for line in list_lines:
            lines_json.append({"lat" : line[0], "lon" : line[1]})   
        db_lock.release()         
        return lines_json
    except Exception as e:
        db_lock.release()
        raise HTTPException(status_code=401, detail=str(e))
    
    
    