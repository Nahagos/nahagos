from fastapi import FastAPI, HTTPException, Response, Depends, Cookie
from pydantic import BaseModel
from datetime import datetime
import uuid
from db import Database

app = FastAPI()
class Station(BaseModel):
    id: int
    name: str
    coords: tuple

class PassengerRequest(BaseModel):
    username: str
    password: str

class DriverLogin(BaseModel):

    username: str
    password: str

connected_users = {}

connected_drivers = {}

registered_trips = {}

db = Database("db.sql")

@app.get("/")
def root():
    return {"message": "Server Is Legit, V1.0.0"}


@app.get("/lines-from-station/{station_id}")
def get_lines_starting(station_id: int):
    """
    Retrieve a list of bus lines that depart from a given station.
    """

    line1 = {"line_id": 12342332, "line_num": 18, "name":"Tel aviv to jerualem", "operator":"Eged", "schedualed_arrival_time":"12:45","live_arrival_time":"12:48:30", "Nahagos":True}
    line2 = {"line_id": 123423, "line_num": 32, "name":"Haifa to jerualem", "operator":"Metropolin", "schedualed_arrival_time":"10:40","live_arrival_time":None, "Nahagos":False}
    return {"lines":[line1, line2]}


@app.get("/lines-by-station/{stop_id}")
def get_real_time_lines(stop_id: int):
    """
    Retrives real-time arriving times at given station
    """   
    try:
        return db.get_lines_by_station(stop_id)
    except Exception as e:
        raise HTTPException(status_code=401, detail=str(e))

    # line1 = {"line_id": 12342332, "line_num": 18, "name":"Tel aviv to jerualem", "operator":"Eged", "schedualed_arrival_time":"12:45","live_arrival_time":"12:48:30", "Nahagos":True}
    # line2= {"line_id": 123423, "line_num": 32, "name":"Haifa to jerualem", "operator":"Metropolin", "schedualed_arrival_time":"10:40","live_arrival_time":None, "Nahagos":False} 


@app.get("/update-arrival-time/{station_id, bus_id}")
def update_arrival_time(station_id: int, bus_id: int):
    """
    Get real-time bus location and arrival time to a given station
    """

    return {"Arriving": "12:54:32", "Location": {"Lat": "33.4234242", "Lon": "34.334211"}}


@app.post("/passenger/wait-for/")
def passenger_wait_for_bus(stop_id: int, trip_id: int, time: str):
    """
    Log that a passenger is waiting for a specific bus at a given station.
    """
    if db.check_stop_on_trip(stop_id, trip_id):
        if registered_trips[trip_id] is not None:
            registered_trips[trip_id][1].appdend(stop_id)
            return {"message": "Passenger wait request logged successfully"}
        raise HTTPException(status_code=401, detail="No Nahagos!")

    
    raise HTTPException(status_code=401, detail="Stop is not in this lines route")
    


@app.post("/driver/drive/")
def register_for_line(trip_id: int, cookies_and_milk: str = Cookie(None)):
    """
    Register a driver for a specific line
    """
    # TODO: change the status of nahagos in this specific line and fix checks for validation of line

    driver = connected_drivers.get(cookies_and_milk)
    if not driver:
        raise HTTPException(status_code=401, detail="Unauthorized. Please log in as a driver.")
    if not db.check_schedule(trip_id):
        raise HTTPException(status_code=401, detail="Line isn't schedualed for you")
    
    registered_trips[trip_id] = driver, []

    return {"message": "Line registered successfully"}


@app.delete("/driver/drive")
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


@app.post("/driver/login/")
def driver_login(driver: DriverLogin, response: Response):
    """
    Check if the id, username and the password are correct
    """

    if db.login_driver(driver.id, driver.username, driver.password):
        session_id = str(uuid.uuid4())  # Generate a unique session ID
        connected_drivers[session_id] = {"id": id}
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

    # Uncomment the following code for actual login logic
    # if db.login_passenger(username, password):
    #     session_id = str(uuid.uuid4())
    #     connected_drivers[session_id] = {"username": username}
    #     response.set_cookie(key="session_id", value=session_id, httponly=True)
    #     return {"message": "Login successful"}
    # else:
    #     raise HTTPException(status_code=401, detail="Invalid credentials")

    session_id = str(uuid.uuid4())
    response.set_cookie(key="cookies_and_milk", value=session_id, httponly=True)
    return {"message": "Login successful"}



@app.post("/passenger/signup/")
def passenger_signup(request: PassengerRequest, response: Response):
    """
    Check if the username and the password can be register, and if so register
    """
    username = request.username
    password = request.password
    if db.signup_passenger(username, password):
        session_id = str(uuid.uuid4())  # Generate a unique session ID
        connected_drivers[session_id] = {"username": username}
        response.set_cookie(key="cookies_and_milk", value=session_id, httponly=True)  # Set session ID in a secure cookie
        return {"message": "Login successful"}
    else:
        raise HTTPException(status_code=401, detail="Invalid signup")



@app.get("/driver/schedule")
def get_driver_schedule(cookies_and_milk: str = Cookie(None)):
    """
    Get daily schedule of driver
    """
    driver = connected_drivers.get(cookies_and_milk)
    if not driver:
        raise HTTPException(status_code=401, detail="Unauthorized. Please log in as a driver.")
    return db.get_driver_schedule(driver)