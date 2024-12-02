from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()
class Station(BaseModel):
    id: int
    name: str
    cords: tuple



@app.get("/")
def root():
    return {"message": "Server Is Legit, V1.0.0"}

@app.get("/lines-from-station/{station_id}")
def get_lines_starting(station_id: int):
    """
    Retrieve a list of bus lines that depart from a given station.
    """
    line1 = {"line_id": 45634345}
    line2= {"line_id": 12341523}
    return {"lines":[line1, line2]}



@app.get("/lines-by-station/{station_id}")
def get_real_time_lines(station_id: int):
    """
    Retrives real-time arriving times at given station
    """   

    line1 = {"line_id": 45634345, "bus_id": 1234, "Arrival Time": "12:41:23"}
    line2= {"line_id": 4242234, "bus_id": 4432, "Arrival Time": "22:21:43"}
    return {"lines":[line1, line2]}

@app.get(" /update-arrival-time/{station_id, bus_id}")
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

    # Store the entry (in-memory for now)
    #waiting_passengers.append(station_id, bus_id))

    return {"message": "Passenger wait request logged successfully"}

@app.post("/driver/drive/")
def register_for_line(user_id: str, line_id: str, dep_time: str):
    """
    Register a driver for a specific line
    """

    # change the status of nahagos in this specific line

    return {"message": "Line registered successfully"}

@app.delete("/driver/drive")
def delete_drive(user_id: str, line_id: str, dep_time: str):
    """
    Delete a drive 
    """
    # change the status of nahagos in this specific line

    return {"message": "Drive was deleted successfully"}


@app.get("/update-station-list/{last_updated_date}")
def update_station_list(last_updated_date: str):
    """
    Check whether or not the station list is up to date, and if not sending changes
    """
     # Compare last_updated_date to last date the station list was updated
    up_to_date = True
    if up_to_date: 
        return {"status": "Up to date"}
    else: 
        return {"status": "Not up to date", "changes": []}

@app.post("/driver/login")
def driver_login(username: str, password: str):
    """
    Check if the username and the password are correct
    """
    return {"message", "200 OK"}

@app.post("/passenger/login")
def passenger_login(username: str, password: str):
    """
    Check if the username and the password are correct
    """
    return {"message", "200 OK"}

@app.post("/driver/signup")
def driver_signup(username: str, password: str):
    """
    Check if the username and the password can be register, and if so register
    """
    return {"message", "200 OK"}

@app.post("/passenger/signup")
def passenger_signup(username: str, password: str):
    """
    Check if the username and the password can be register, and if so register
    """
    return {"message", "200 OK"}
