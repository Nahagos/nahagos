import os
import requests
from pathlib import Path
import tempfile
import sqlite3
import hashlib
from datetime import datetime, timedelta, timezone
import zipfile



class Database:
    def __init__(self, db_name):
        cool = not os.path.exists('db.sql')
        self.connection = sqlite3.connect(db_name)
        self.cursor = self.connection.cursor()
        if cool:
            self.download_and_init_gtfs()
    
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
    
    def download_and_init_gtfs(self):
        """
        Downloads the Israel public transportation GTFS file,
        initializes the database, and cleans up the downloaded file.
        """
        # URL for GTFS data
        url = "https://gtfs.mot.gov.il/gtfsfiles/israel-public-transportation.zip"
        
        # Temporary directory for downloading and extracting files
        with tempfile.TemporaryDirectory() as tmp_dir:
            zip_path = os.path.join(tmp_dir, "gtfs.zip")
            
            # Download GTFS ZIP file
            response = requests.get(url, stream=True, verify=False)
            if response.status_code == 200:
                with open(zip_path, 'wb') as f:
                    f.write(response.content)
                print("GTFS file downloaded successfully.")
            else:
                raise Exception("Failed to download GTFS file. HTTP Status Code:", response.status_code)
            
            # Initialize database with extracted files
            self.initialize_db(zip_path)

    def initialize_db(self, zip_path):
        self.create_tables()
        
        #adding default users        
        self.signup_passenger("user1", "password123")
        self.add_driver("d1", "p1", 6151181, "Alice Johnson", "ABC1234")
        self.add_driver("driver02", "driver02", 1234567, "Chris Lee", "XYZ5678")
        self.add_driver("driver03", "password123", 1522484, "Maria Davis", "LMN3456")
        self.add_things_to_schedule()
        self.open()

        dir_path = zip_path.replace(".zip", "/")
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
        self.close()
    
    def create_tables(self):
        self.open()
        self.cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS passengers (
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
                driver_id INTEGER NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                license_plate TEXT NOT NULL
            );
            """
        )
        self.cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS schedule (
                name TEXT,
                line TEXT,
                trip_id TEXT,
                driver_id INT,
                day TEXT,
                hour TEXT, -- hh:mm
                PRIMARY KEY (line, trip_id, driver_id),
                FOREIGN KEY (driver_id) REFERENCES drivers(driver_id)
            );
            """
        )
        self.connection.commit()
        self.close()
        
    def add_driver(self, username, password, driver_id, name, license_plate):
        try:
            self.open()
            hashed_password = hashlib.sha256(password.encode()).hexdigest()
            self.cursor.execute("""
                INSERT INTO drivers (username, password, driver_id, name, license_plate)
                VALUES (?, ?, ?, ?, ?)
            """, (username, hashed_password, driver_id, name, license_plate))
            self.connection.commit()
            self.close()
            return True
        except sqlite3.IntegrityError:
            self.close()
            return False
        
    def add_things_to_schedule(self):
        self.add_to_schedule('fake_line', 6151181, '12', '5656648_311224', 'sunday', '06:40')
        self.add_to_schedule('fake_line', 1234567, '8', '17332096_261224', 'sunday', '18:00')
        self.add_to_schedule('fake_line', 1522484, '8', '2568376_261224', 'sunday', '17:20')
        self.add_to_schedule('fake_line', 6151181, '8', '2568332_011224', 'sunday', '10:55')
        self.add_to_schedule('fake_line', 1234567, '8', '17332309_261224', 'sunday', '07:50')
        self.add_to_schedule('fake_line', 1522484, '8', '13077356_011224', 'sunday', '18:40')
        self.add_to_schedule('fake_line', 6151181, '46', '27660227_261224', 'sunday', '08:30')
        self.add_to_schedule('fake_line', 1234567, '46', '3148_261224', 'sunday', '08:05')
        self.add_to_schedule('fake_line', 1522484, '46', '47974694_251224', 'sunday', '06:40')
        self.add_to_schedule('fake_line', 6151181, '56', '26493025_311224', 'sunday', '15:45')
        self.add_to_schedule('fake_line', 1234567, '57', '584632122_011224', 'sunday', '13:45')
        self.add_to_schedule('fake_line', 1522484, '57', '585422673_011224', 'sunday', '10:45')
        self.add_to_schedule('fake_line', 6151181, '57', '3559_261224', 'sunday', '12:35')
        self.add_to_schedule('fake_line', 1234567, '57', '56445279_261224', 'sunday', '12:20')


        
    def add_to_schedule(self, name, driver_id, line, trip_id, day, hour):
        try:
            self.open()
            self.cursor.execute("""
                INSERT INTO schedule (name, driver_id, line, trip_id, day, hour)
                VALUES (?, ?, ?, ?, ?, ?)
            """, (name, driver_id, line, trip_id, day, hour))
            self.connection.commit()
            self.close()
            return True
        except sqlite3.IntegrityError:
            self.close()
            return False
        
    def close(self):
        self.connection.close()
    
    def open(self):
        self.connection = sqlite3.connect('db.sql')
        self.cursor = self.connection.cursor()

    def signup_passenger(self, username, password):
        try:
            self.open()
            self.cursor.execute("INSERT INTO passengers (username, password) VALUES (?, ?)", (username, hashlib.sha256(password.encode()).hexdigest()))
            self.connection.commit()
            self.close()
            return True
        except sqlite3.IntegrityError:
            self.close()
            return False

    def login_passenger(self, username, password):
        self.open()
        self.cursor.execute("SELECT * FROM passengers WHERE username = ? AND password = ?", (username, hashlib.sha256(password.encode()).hexdigest()))
        user = self.cursor.fetchone()
        self.close()
        return user is not None

    def login_driver(self, username, password, driver_id):
        self.open()
        self.cursor.execute("SELECT * FROM drivers WHERE username = ? AND password = ? AND driver_id = ?", (username, hashlib.sha256(password.encode()).hexdigest(), driver_id))
        user = self.cursor.fetchone()
        self.close()
        return user is not None

    def check_line_day(self, route_id):
        self.open()
        self.cursor.execute("SELECT service_id FROM trips WHERE route_id = ?",(route_id,))
        service_id = self.cursor.fetchone()
        if not service_id:
            self.close()
            return False                        # Wrong route_id
        current_day_name = datetime.now().strftime('%A').lower()
        self.cursor.execute("SELECT ? FROM calander WHERE service_id = ?",(current_day_name, service_id))
        service_id = self.cursor.fetchone()
        self.close()
        return service_id[0] == '1' 

    def get_lines_by_station(self, stop_id):
        self.open()
        gmt3 = timezone(timedelta(hours=2))
        now = datetime.now(gmt3)
        current_day = now.weekday()
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
        current_time = now.strftime("%H:%M:%S")
        print(current_time)
        self.cursor.execute(f"""
            SELECT trip_id, departure_time, route_long_name, route_short_name, agency_name, stop_lat, stop_lon
            FROM stop_times
            NATURAL JOIN trips
            NATURAL JOIN calendar
            NATURAL JOIN routes
            NATURAL JOIN agency
            NATURAL JOIN stops
            WHERE stop_id = ? 
            AND departure_time > ? 
            AND {day_column} = 1
            GROUP BY route_long_name, route_short_name
            ORDER BY departure_time;

        """, (stop_id, current_time))
        lines = self.cursor.fetchall()
        self.close()
        return lines    
    
    def check_stop_on_trip(self, trip_id, stop_id):
        self.open()
        self.cursor.execute(f"""
                    SELECT stop_id
                    FROM stop_times
                    WHERE stop_id = ? AND
                    trip_id = ?
                    """, (stop_id, trip_id))
        res = self.cursor.fetchall()
        self.close()
        return res is not None
    
    def get_stops_by_trip_id(self, trip_id):
        self.open()
        self.cursor.execute(f"""
            SELECT stop_id, stop_name, departure_time, stop_lat, stop_lon
            FROM stop_times NATURAL JOIN stops
            WHERE trip_id = ?
            """, (trip_id,))
        stops = self.cursor.fetchall()
        self.close()
        return stops

    def get_driver_schedule(self, driver_id):
        self.open()
        self.cursor.execute('SELECT day, trip_id, line, hour, name FROM schedule WHERE driver_id = ?', (driver_id,))
        schedule = self.cursor.fetchall()
        self.close()
        return schedule
    
    def get_trip_shape(self, trip_id):
        self.open()
        self.cursor.execute('SELECT shape_id FROM trips where trip_id = ?', (trip_id,))
        shape_id = self.cursor.fetchone()
        if shape_id:
            self.cursor.execute('SELECT shape_pt_lat, shape_pt_lon FROM shapes where shape_id = ?', (shape_id[0],))
            shape = self.cursor.fetchall()
        else:
            shape = []
        self.close()
        return shape
    
    def check_schedule(self, trip_id, driver_id):
        self.open()
        self.cursor.execute('SELECT day FROM schedule where trip_id = ? and driver_id = ?', (trip_id, driver_id))
        res = self.cursor.fetchone()
        self.close()
        if not res:
            return False
        return res[0] == datetime.now().strftime("%A").lower()
        

if __name__ == "__main__":
    db = Database("db.sql")
    # print(db.sign_up("testuser", "testpass"))
    # print(db.check_user("testuser", "testpass"))
    a=db.get_lines_by_station(12193)
    for row in a:
        print(row)
    print(len(a))
    db.close()
