from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()
class Line(BaseModel):
    number: int
    title: str
    operator: str
    id: str


@app.get("/")
def root():
    return {"message": "Server Is Legit, V1.0.0"}

@app.get("/lines-from-station/{station_id}", )
def get_lines_starting(station_id: str):
    """
    Retrieve a list of bus lines that depart from a given station.
    """
    line1 = {"number": 1, "title": "Jerusalem to Natania", "operator": "Kavim", "id": "x8xgos8fxo"}
    line2= {"number": 2, "title": "Tel Aviv to Haifa", "operator": "Egged", "id": "n4jx30sk29"}
    return {"lines":[line1, line2]}

