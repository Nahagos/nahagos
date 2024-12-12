import sqlite3
import hashlib
from datetime import datetime

class Database:
    def __init__(self, db_name):
        self.connection = sqlite3.connect(db_name)
        self.cursor = self.connection.cursor()
        self.initialize_db(r"C:\Users\Epsilon\Downloads\israel-public-transportation.zip")

    def initialize_db(self, zip_path):
        db.cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS passangers (
                username TEXT NOT NULL PRIMARY KEY,
                password TEXT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS drivers (
                username TEXT NOT NULL,
                password TEXT NOT NULL,
                driver_id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                license_plate TEXT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS stops (
                stop_desc TEXT PRIMARY KEY,
                stop_name TEXT NOT NULL,
                stop_lat REAL NOT NULL,
                stop_lon REAL NOT NULL
            );
            """
        )
        dir_path = zip_path.replace(".zip", "\\")
        with zipfile.ZipFile(zip_path, 'r') as zip_ref:
            zip_ref.extractall(dir_path)
        files_names = ["calendar", "routes", "shapes", "stop_times", "stops", "trips"]
        for file_name in files_names:
            path = dir_path + file_name + ".txt"
            with open(path, 'r', encoding="utf8") as file:
                # Read first line in the file
                line1 = file.readline().strip()

                # Create table
                self.cursor.execute("CREATE TABLE IF NOT EXISTS " + file_name + " (" + line1.replace(",", ", ") + ")")

                # Read each line in the file and save list of params to insert:
                request = ("INSERT INTO %s VALUES (" + ("?, " * len(line1.split(",")))[:-2] + ")") % (file_name)
                request_params = []
                for line in file:
                    # parse each line
                    line = line.strip().split(",")
                    for var in line:
                        if var == '':
                            var = " "
                    request_params += [line]

                # Insert a row of data
                self.cursor.executemany(request, request_params)

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

    def login_driver(self, username, password, driver_id)
        self.cursor.execute("SELECT * FROM drivers WHERE username = ? AND password = ? AND driver_id = ?", (username, hashlib.sha256(password.encode()), driver_id))
        user = self.cursor.fetchone()
        return user is not None

    def check_line_day(route_id)
        self.cursor.execute("SELECT service_id FROM trips WHERE route_id = ?",(route_id,))
        service_id = self.cursor.fetchone()
        if not service_id:
            return False                        # Wrong route_id
        current_day_name = datetime.now().strftime('%A').lower()
        self.cursor.execute("SELECT ? FROM calander WHERE service_id = ?",(current_day_name, service_id))
        service_id = self.cursor.fetchone()
        return service_id[0] == '1' 
        

if __name__ == "__main__":
    db = Database("users.db")
    check_line_day('')
    # print(db.sign_up("testuser", "testpass"))
    # print(db.check_user("testuser", "testpass"))
    db.close()
