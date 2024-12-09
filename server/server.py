from fastapi import FastAPI, HTTPException, Response, Depends, Cookie
from pydantic import BaseModel
from datetime import datetime
import uuid

app = FastAPI()
class Station(BaseModel):
    id: int
    name: str
    coords: tuple

connected_users = {}

connected_drivers = {}

registered_trips = {}

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


@app.get("/lines-by-station/{station_id}")
def get_real_time_lines(station_id: int):
    """
    Retrives real-time arriving times at given station
    """   

    line1 = {"line_id": 12342332, "line_num": 18, "name":"Tel aviv to jerualem", "operator":"Eged", "schedualed_arrival_time":"12:45","live_arrival_time":"12:48:30", "Nahagos":True}
    line2= {"line_id": 123423, "line_num": 32, "name":"Haifa to jerualem", "operator":"Metropolin", "schedualed_arrival_time":"10:40","live_arrival_time":None, "Nahagos":False}
    return {"lines":[line1, line2]}


@app.get("/update-arrival-time/{station_id, bus_id}")
def update_arrival_time(station_id: int, bus_id: int):
    """
    Get real-time bus location and arrival time to a given station
    """

    return {"Arriving": "12:54:32", "Location": {"Lat": "33.4234242", "Lon": "34.334211"}}


@app.post("/passenger/wait-for/")
def passenger_wait_for_bus(station_id: str, bus_id: str):
    """
    Log that a passenger is waiting for a specific bus at a given station.
    """

    # TODO: Store the entry (in-memory for now)
    #waiting_passengers.append(station_id, bus_id))

    return {"message": "Passenger wait request logged successfully"}


@app.post("/driver/drive/")
def register_for_line(route_id: int, time: str, session_id: str = Cookie("session_id")):
    """
    Register a driver for a specific line
    """
    # TODO: change the status of nahagos in this specific line and fix checks for validation of line

    driver = connected_drivers.get(session_id)
    if not driver:
        raise HTTPException(status_code=401, detail="Unauthorized. Please log in as a driver.")
    if not db.check_line_day(route_id):
        raise HTTPException(status_code=401, detail="Line isn't schedualed today")
    # Check if the provided time is in the future
    try:
        departure_time = datetime.strptime(time, "%H:%M:%S")
    except ValueError:
        raise HTTPException(status_code=400, detail="Invalid time format. Use 'HH:MM:SS'.")

    if departure_time < datetime.now():
        raise HTTPException(status_code=400, detail="Line has already left the station.")





    return {"message": "Line registered successfully"}


@app.delete("/driver/drive")
def delete_drive(user_id: str, line_id: str, dep_time: str):
    """
    Delete a drive 
    """

    # TODO: change the status of nahagos in this specific line

    return {"message": "Drive was deleted successfully"}


@app.get("/update-station-list/{last_updated_date}")
def update_station_list(last_updated_date: str):
    """
    Check whether or not the station list is up to date, and if not sending changes
    """

     # TODO: Compare last_updated_date to last date the station list was updated
    up_to_date = True
    if up_to_date: 
        return {"status": "Up to date"}
    else: 
        return {"status": "Not up to date", "changes": []}


@app.post("/driver/login")
def driver_login(username: str, password: str, id: str, response: Response):
    """
    Check if the id, username and the password are correct
    """
    if db.login_driver(id, username, password):
        session_id = str(uuid.uuid4())  # Generate a unique session ID
        connected_drivers[session_id] = {"id": id}
        response.set_cookie(key="session_id", value=session_id, httponly=True)  # Set session ID in a secure cookie
        return {"message": "Login successful"}
    else:
        raise HTTPException(status_code=401, detail="Invalid credentials")


@app.post("/passenger/login")
def passenger_login(username: str, password: str, response: Response):
    """
    Check if the username and the password are correct
    """
    if db.login_passenger(username, password):
        session_id = str(uuid.uuid4())  # Generate a unique session ID
        connected_drivers[session_id] = {"username": username}
        response.set_cookie(key="session_id", value=session_id, httponly=True)  # Set session ID in a secure cookie
        return {"message": "Login successful"}
    else:
        raise HTTPException(status_code=401, detail="Invalid credentials")


@app.post("/passenger/signup")
def passenger_signup(username: str, password: str, response: Response):
    """
    Check if the username and the password can be register, and if so register
    """
    if db.signup_passenger(username, password):
        session_id = str(uuid.uuid4())  # Generate a unique session ID
        connected_drivers[session_id] = {"username": username}
        response.set_cookie(key="session_id", value=session_id, httponly=True)  # Set session ID in a secure cookie
        return {"message": "Login successful"}
    else:
        raise HTTPException(status_code=401, detail="Invalid signup")
