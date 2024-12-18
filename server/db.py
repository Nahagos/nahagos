import sqlite3
import hashlib
from datetime import datetime
import zipfile
import os

class Database:
    def __init__(self, db_name):
        self.connection = sqlite3.connect(db_name)
        self.cursor = self.connection.cursor()
        if not os.path.exists('users.db'):
            self.initialize_db(r"C:\Users\Epsilon\Downloads\israel-public-transportation.zip")
        
    def parse_file(self, path, file_name):
        """
        Parse the file and insert the data into the database
        :param cursor: cursor object
        :param path: path to the file to read data from
        :param file_name: table name
        :return: none
        """
        # Open the files in read mode and convert them to sql format
        with open(path, 'r', encoding="utf8") as file:
            # Read first line in the file
            line1 = file.readline().strip()

            # Read each line in the file and save list of params to insert:
            request = ("INSERT INTO %s VALUES (" + ("?, " * len(line1.split(",")))[:-2] + ")") % (file_name)
            request_params = []

            print("line1", line1)
            for line in file:
                # parse each line

                line = line.strip().split(",")
                for var in line:
                    if var == '':
                        var = None

                request_params += [line]

            # Insert a row of data
            self.cursor.executemany(request, request_params)

    def initialize_db(self, zip_path):
        self.cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS passangers (
                username TEXT NOT NULL PRIMARY KEY,
                password TEXT NOT NULL
            );
            """
        )
        self.cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS drivers (
                username TEXT NOT NULL,
                password TEXT NOT NULL,
                driver_id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                license_plate TEXT NOT NULL
            );
            """
        )

        dir_path = zip_path.replace(".zip", "\\")
        with zipfile.ZipFile(zip_path, 'r') as zip_ref:
            zip_ref.extractall(dir_path)

        # Create tables columns names:
        files_names = {"stops": ["stop_id INTEGER not null PRIMARY KEY", "stop_code INTEGER not null", "stop_name TEXT not null", "stop_desc TEXT not null", "stop_lat REAL not null", "stop_lon REAL not null", "location_type INTEGER not null", "parent_station INTEGER", "zone_id INTEGER not null"],
                    "agency": ["agency_id INTEGER PRIMARY KEY", "agency_name TEXT", "agency_url TEXT", "agency_timezone TEXT", "agency_lang TEXT", "agency_phone TEXT", "agency_fare_url TEXT"],
                    "calendar": ["service_id INTEGER PRIMARY KEY", "sunday INTEGER", "monday INTEGER", "tuesday INTEGER", "wednesday INTEGER", "thursday INTEGER", "friday INTEGER", "saturday INTEGER", "start_date INTEGER", "end_date INTEGER"],
                    "routes": ["route_id INTEGER PRIMARY KEY", "agency_id INTEGER", "route_short_name TEXT", "route_long_name INTEGER", "route_desc TEXT", "route_type INTEGER", "route_color"],
                    "shapes": ["shape_id INTEGER", "shape_pt_lat REAL", "shape_pt_lon REAL", "shape_pt_sequence INTEGER"],
                    "stop_times": ["trip_id TEXT", "arrival_time TEXT", "departure_time TEXT", "stop_id INTEGER", "stop_sequence INTEGER", "pickup_type INTEGER", "drop_off_type INTEGER", "shape_dist_traveled TEXT"],
                    "trips": ["route_id INTEGER", "service_id INTEGER", "trip_id TEXT", "trip_headsign TEXT", "direction_id INTEGER", "shape_id INTEGER", "wheelchair_accessible INTEGER"]}

        # Parse each file:
        for file_name in files_names:
            self.cursor.execute(
                "CREATE TABLE IF NOT EXISTS " + file_name + " (" + ", ".join([var for var in files_names[file_name]]) + ")")
            self.parse_file(dir_path + file_name + ".txt", file_name)

            # save (commit) the changes
            self.connection.commit()


        # Save (commit) the changes
        self.connection.commit()
        
    def close(self):
        self.connection.close()

    def signup_passanger(self, username, password):
        try:
            self.cursor.execute("INSERT INTO passangers (username, password) VALUES (?, ?)", (username, hashlib.sha256(password.encode())))
            self.connection.commit()
            return True
        except sqlite3.IntegrityError:
            return False

    def login_passanger(self, username, password):
        self.cursor.execute("SELECT * FROM passangers WHERE username = ? AND password = ?", (username, hashlib.sha256(password.encode())))
        user = self.cursor.fetchone()
        return user is not None

    def login_driver(self, username, password, driver_id):
        self.cursor.execute("SELECT * FROM drivers WHERE username = ? AND password = ? AND driver_id = ?", (username, hashlib.sha256(password.encode()), driver_id))
        user = self.cursor.fetchone()
        return user is not None

    def check_line_day(self, route_id):
        self.cursor.execute("SELECT service_id FROM trips WHERE route_id = ?",(route_id,))
        service_id = self.cursor.fetchone()
        if not service_id:
            return False                        # Wrong route_id
        current_day_name = datetime.now().strftime('%A').lower()
        self.cursor.execute("SELECT ? FROM calander WHERE service_id = ?",(current_day_name, service_id))
        service_id = self.cursor.fetchone()
        return service_id[0] == '1' 

    def lines_from_station(self, stop_id):
        # Get current day and hour
        now = datetime.now()
        current_day = now.weekday()  # Monday is 0, Sunday is 6
        current_hour = now.hour

        # Convert to the correct format for the SQL query
        day_mapping = {
            0: 'monday',
            1: 'tuesday',
            2: 'wednesday',
            3: 'thursday',
            4: 'friday',
            5: 'saturday',
            6: 'sunday'
        }
        
        day_column = day_mapping[current_day]
        current_time = now.strftime("%H:%M:%S")  # Get current time in HH:MM:SS format

        # Execute query with dynamic day and time
        lines = []
        self.cursor.execute(f"""
                            SELECT trip_id, departure_time, route_long_name, route_short_name, agency_name
                            FROM stop_times 
                            NATURAL JOIN trips 
                            NATURAL JOIN calendar 
                            NATURAL JOIN routes 
                            NATURAL JOIN agency
                            WHERE stop_id = {stop_id} 
                            AND departure_time > "{current_time}" 
                            AND {day_column} = 1
                            """)
        return self.cursor.fetchall()

        

        
        

if __name__ == "__main__":
    db = Database("users.db")
    # print(db.sign_up("testuser", "testpass"))
    # print(db.check_user("testuser", "testpass"))
    print(db.lines_from_station(1234))
    db.close()
