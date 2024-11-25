from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional

app = FastAPI()

# Data Models
class BusSchedule(BaseModel):
    bus_id: str
    line: str
    next_stop: str
    arrival_time: str

class Location(BaseModel):
    latitude: float
    longitude: float

class BusLocationUpdate(BaseModel):
    bus_id: str
    location: Location

class StopRequest(BaseModel):
    bus_id: str
    station: str
    user_id: str

# In-Memory Database
bus_schedules = {}
bus_locations = {}
stop_requests = []

# Endpoints
@app.get("/bus-schedule/{line}", response_model=List[BusSchedule])
def get_bus_schedule(line: str):
    """
    Get real-time bus schedules for a given line.
    """
    schedules = [schedule for schedule in bus_schedules.values() if schedule.line == line]
    if not schedules:
        raise HTTPException(status_code=404, detail="No schedules found for this line")
    return schedules

@app.get("/bus-location/{bus_id}", response_model=Location)
def get_bus_location(bus_id: str):
    """
    Get real-time location of a specific bus.
    """
    location = bus_locations.get(bus_id)
    if not location:
        raise HTTPException(status_code=404, detail="Bus location not found")
    return location

@app.post("/bus-location/")
def update_bus_location(update: BusLocationUpdate):
    """
    Update the location of a bus.
    """
    bus_locations[update.bus_id] = update.location
    return {"message": "Location updated successfully"}

@app.post("/stop-request/")
def request_stop(request: StopRequest):
    """
    Allow users to request a stop at a given station.
    """
    stop_requests.append(request)
    return {"message": "Stop request received"}

@app.get("/stop-requests/{bus_id}", response_model=List[StopRequest])
def get_stop_requests(bus_id: str):
    """
    Retrieve stop requests for a specific bus.
    """
    bus_stop_requests = [req for req in stop_requests if req.bus_id == bus_id]
    return bus_stop_requests

# Example Endpoint to Add a Schedule (for Admin)
@app.post("/add-bus-schedule/")
def add_bus_schedule(schedule: BusSchedule):
    """
    Add a new bus schedule.
    """
    bus_schedules[schedule.bus_id] = schedule
    return {"message": "Bus schedule added successfully"}
