from fastapi import FastAPI, HTTPException, Response, Depends, Cookie
from pydantic import BaseModel
from datetime import datetime
import uuid
from db import Database
import threading

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

connected_users = {}

connected_drivers = {}      # driver_cookie : driver_id, last_action, trip_id

registered_trips = {}       # trip_id : [stops_list]

db = Database("db.sql")

def driver_connectivity():
    for driver, properties in connected_drivers.items():
        if (datetime.now() - properties[1]) > datetime.timedelta(minutes=5):
            del registered_trips[properties[2]]
            del connected_drivers[driver]

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
 

@app.get("/lines-by-station/{stop_id}")
def get_real_time_lines(stop_id: int, cookies_and_milk :str = Cookie(None)):
    """
    Retrives real-time arriving times at given station
    """  
    # validate user
    if not cookies_and_milk or cookies_and_milk not in connected_users:
        raise HTTPException(status_code=401, detail="User not authenticated")
    
    try:
        list_lines = db.get_lines_by_station(stop_id)
        lines_json = []
        for line in list_lines:
            lines_json.append({"trip_id": line[0], "departure": line[1], "name": line[2], "line_num": line[3], "operator": line[4]})            
        return {"lines": lines_json}
    except Exception as e:
        raise HTTPException(status_code=401, detail=str(e))


@app.get("/update-arrival-time/{station_id, bus_id}")
def update_arrival_time(station_id: int, bus_id: int):
    """
    Get real-time bus location and arrival time to a given station
    """

    return {"Arriving": "12:54:32", "Location": {"Lat": "33.4234242", "Lon": "34.334211"}}


@app.post("/passenger/wait-for/")
def passenger_wait_for_bus(stop_id: int, trip_id: int, cookies_and_milk:str = Cookie(None)):
    """
    Log that a passenger is waiting for a specific bus at a given station.
    """
     # Validate user session
    if not cookies_and_milk or cookies_and_milk not in connected_users:
        raise HTTPException(status_code=401, detail="User not authenticated")
    
    if db.check_stop_on_trip(trip_id, stop_id):
        if trip_id in registered_trips.keys:
            registered_trips[trip_id].add(stop_id)
            return {"message": "Passenger wait request logged successfully"}
        raise HTTPException(status_code=401, detail="No Nahagos!")

    
    raise HTTPException(status_code=401, detail="Stop is not in this lines route") 



@app.post("/driver/drive/register/")
def register_for_line(trip_id: int, cookies_and_milk: str = Cookie(None)):
    """
    Register a driver for a specific line
    """
    # TODO: change the status of nahagos in this specific line and fix checks for validation of line

    driver = connected_drivers.get(cookies_and_milk)
    if not driver:
        raise HTTPException(status_code=401, detail="Unauthorized. Please log in as a driver.")
    
    update_last_active(cookies_and_milk)
    
    if not db.check_schedule(trip_id):
        raise HTTPException(status_code=401, detail="Line isn't schedualed for you")
    
    registered_trips[trip_id] = set()

    return {"message": "Line registered successfully"}


@app.delete("/driver/drive/delete/")
def delete_drive(user_id: str, line_id: str, dep_time: str):
    """
    Delete a drive 
    """

    # TODO: change the status of nahagos in this specific line

    return {"message": "Drive was deleted successfully"}


@app.get("/update-station-list/{last_updated_date}")
def update_station_list(last_updated_date: str, cookies_and_milk: str = Cookie(None)):
    """
    Check whether or not the station list is up to date, and if not sending changes
    """
    try:
        # Validate user session
        if not cookies_and_milk or cookies_and_milk not in connected_drivers:
            raise HTTPException(status_code=401, detail="User not authenticated")
        
        # Validate date format
        try:
            last_updated_date = datetime.strptime(last_updated_date, "%Y-%m-%d")
        except ValueError:
            raise HTTPException(status_code=400, detail="Invalid date format. Use YYYY-MM-DD.")


        # Query the database to check for diffs since the last_updated_date
        diffs = db.get_diffs_since_date(last_updated_date)  # Custom function to fetch diffs

        if not diffs:  # If no diffs are found, the station list is up-to-date
            return {"status": "Up to date"}

        return {"status": "Not up to date", "changes": diffs}

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"An error occurred: {str(e)}")

        if not diffs:  # If no diffs are found, the station list is up-to-date
            return {"status": "Up to date"}

        return {"status": "Not up to date", "changes": diffs}

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"An error occurred: {str(e)}")

@app.post("/driver/login/")
def driver_login(driver: DriverLogin, response: Response):
    """
    Check if the id, username and the password are correct
    """
    if db.login_driver(driver.username, driver.password, driver.id):
        session_id = str(uuid.uuid4())  # Generate a unique session ID
        connected_drivers[session_id] = [driver.id, None, None]
        
        response.set_cookie(key="cookies_and_milk", value=session_id, httponly=True)  # Set session ID in a secure cookie
        return {"message": "Login successful"}
    else:
        raise HTTPException(status_code=401, detail="Invalid credentials")


@app.post("/passenger/login/")
def passenger_login(request: PassengerRequest, response: Response):
    """
    Check if the username and the password are correct
    """
    # Access the username and password from the request
    username = request.username
    password = request.password

    if db.login_passenger(username, password):
        session_id = str(uuid.uuid4())
        connected_users[session_id] = {"username": username}
        response.set_cookie(key="cookies_and_milk", value=session_id, httponly=True)
        return {"message": "Login successful"}
    else:
        raise HTTPException(status_code=401, detail="Invalid credentials")


@app.post("/passenger/signup/")
def passenger_signup(request: PassengerRequest, response: Response):
    """
    Check if the username and the password can be register, and if so register
    """
    username = request.username
    password = request.password
    if db.signup_passenger(username, password):
        session_id = str(uuid.uuid4())  # Generate a unique session ID
        connected_users[session_id] = {"username": username}
        response.set_cookie(key="cookies_and_milk", value=session_id, httponly=True)  # Set session ID in a secure cookie
        return {"message": "Login successful"}
    else:
        raise HTTPException(status_code=401, detail="Invalid signup")


@app.get("/stops-by-line/{trip_id}")
def get_stops_by_line(trip_id : str, cookies_and_milk :str = Cookie(None)):
    """
    Retrives the stops for a specific line
    """ 
    # validate user
    if not cookies_and_milk or cookies_and_milk not in connected_users:
        raise HTTPException(status_code=401, detail="User not authenticated") 


    try:
        list_lines = db.get_stops_by_trip_id(trip_id)
        lines_json = []
        for line in list_lines:
            lines_json.append({"stop_id": line[0], "stop_name": line[1], "time": line[2], "stop_lat": line[3], "stop_lon": line[4]})            
        return {"stops": lines_json}
    except Exception as e:
        raise HTTPException(status_code=401, detail=str(e))

    
@app.get("/driver/where-to-stop/")
def where_to_stop(cookies_and_milk :str = Cookie(None)):
    """
    Retrives the stops that a specific driver need to stop
    """
    # validate user
    if not cookies_and_milk or cookies_and_milk not in connected_drivers:
        raise HTTPException(status_code=401, detail="User not authenticated")
    
    update_last_active(cookies_and_milk)
    if connected_drivers[cookies_and_milk][2]:
        return {'stops': registered_trips[connected_drivers[cookies_and_milk][2]]}
    return  {'stops' : []}


@app.get("/driver/schedule/")
def get_schedule(cookies_and_milk :str = Cookie(None)):
    """
    Retrives the schedule for a specific driver
    """ 
    # validate user
    if not cookies_and_milk or cookies_and_milk not in connected_drivers:
        raise HTTPException(status_code=401, detail="User not authenticated") 

    update_last_active(cookies_and_milk)
    try:
        list_lines = db.get_driver_schedule(connected_drivers[cookies_and_milk][0])
        lines_json = []
        for line in list_lines:
            lines_json.append({line[0]: [{"trip_id" : line[1], "line_num" : line[2], "departure" : line[3], "name" : line[4]}]})            
        return lines_json
    except Exception as e:
        raise HTTPException(status_code=401, detail=str(e))

