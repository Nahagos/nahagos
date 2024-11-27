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



# @app.get("/lines/{line_id, station_id}")
# def get_line_info(line_id: int, station_id:int):
#     """
#     Retrieve data about a given line: the stations it passes through and its departure times-table 
#     """

#     # todo: write the function


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
