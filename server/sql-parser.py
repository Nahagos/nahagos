import os
import sys
import sqlite3
import zipfile

"""
This script creates an sql database with tables to store data related to transportation services, extracted from a ZIP file containing CSV files.

The database includes the following tables:
- table_name: columns_names
- stops: stop_id, stop_code, stop_name, stop_desc, stop_lat, stop_lon, location_type, parent_station, zone_id
- agency: agency_id, agency_name, agency_url, agency_timezone, agency_lang, agency_phone, agency_fare_url
- calendar: service_id, sunday, monday, tuesday, wednesday, thursday, friday, saturday, start_date, end_date
- routes: route_id, agency_id, route_short_name, route_long_name, route_desc, route_type, route_color
- shapes: shape_id, shape_pt_lat, shape_pt_lon, shape_pt_sequence
- stop_times: trip_id, arrival_time, departure_time, stop_id, stop_sequence, pickup_type, drop_off_type, shape_dist_traveled
- trips: route_id, service_id, trip_id, trip_headsign, direction_id, shape_id, wheelchair_accessible
"""


def parse_file(cursor, path, file_name):
    """
    Parse the CSV file and insert its data into the match table in the database.

    :param cursor: SQLite cursor object for executing database queries.
    :param path: Path to the CSV file to read data from.
    :param file_name: Name of the table to insert the data into.
    :return: None
    """
    # Open the files in read mode and convert them to sql format
    with open(path, 'r', encoding="utf8") as file:
        # Read the header line to determine the number of columns
        line1 = file.readline().strip()

        # Prepare the SQL insert query - any '?' will be replace with real value into the table
        request = ("INSERT INTO %s VALUES (" + ("?, " * len(line1.split(",")))[:-2] + ")") % file_name
        # save list of params to insert in query:
        request_params = []

        # Read and process each line in the file
        for line in file:
            # Split line into columns and replace empty values with None
            line = line.strip().split(",")
            for var in line:
                if var == '':
                    var = None
            request_params.append(line)

        # Execute all the insert querys
        cursor.executemany(request, request_params)

        print("\033[33mtable %s created successfully\033[0m" % file_name) # "\033[33m" is for print in yellow.


def main():
    """
    Main function to extract data from a ZIP file, and create matchess tables in the database.

    :return: None
    """
    # Check for the correct number of arguments
    if len(sys.argv) != 2:
        print("Usage: python script.py <path_to_zip_file>")
        return False

    zip_path = sys.argv[1]
    dir_path = zip_path.replace(".zip", os.sep)  # replace ".zip" with "\\"

    # Try to extract the ZIP file
    try:
        with zipfile.ZipFile(zip_path, 'r') as zip_ref:
            zip_ref.extractall(dir_path)
    except Exception as e:
        print("Failed to extract the ZIP file. Error:", e)
        return False

    # Delete the SQLite database file if it already exists
    try:
        os.remove('sql.db')
    except FileNotFoundError:
        pass

    # Connect to the SQLite database (or create it if it doesn't exist)
    conn = sqlite3.connect('sql.db')
    cursor = conn.cursor()
    os.system('color') # enable print with colors..

    # Define the schema for the database tables
    files_names = {
        "stops": [
            "stop_id INTEGER not null PRIMARY KEY",
            "stop_code INTEGER not null",
            "stop_name TEXT not null",
            "stop_desc TEXT not null",
            "stop_lat REAL not null",
            "stop_lon REAL not null",
            "location_type INTEGER not null",
            "parent_station INTEGER",
            "zone_id INTEGER not null"
        ],
        "agency": [
            "agency_id INTEGER PRIMARY KEY",
            "agency_name TEXT",
            "agency_url TEXT",
            "agency_timezone TEXT",
            "agency_lang TEXT",
            "agency_phone TEXT",
            "agency_fare_url TEXT"
        ],
        "calendar": [
            "service_id INTEGER PRIMARY KEY",
            "sunday INTEGER",
            "monday INTEGER",
            "tuesday INTEGER",
            "wednesday INTEGER",
            "thursday INTEGER",
            "friday INTEGER",
            "saturday INTEGER",
            "start_date INTEGER",
            "end_date INTEGER"
        ],
        "routes": [
            "route_id INTEGER PRIMARY KEY",
            "agency_id INTEGER",
            "route_short_name TEXT",
            "route_long_name INTEGER",
            "route_desc TEXT",
            "route_type INTEGER",
            "route_color"
        ],
        "shapes": [
            "shape_id INTEGER",
            "shape_pt_lat REAL",
            "shape_pt_lon REAL",
            "shape_pt_sequence INTEGER"
        ],
        "stop_times": [
            "trip_id TEXT",
            "arrival_time TEXT",
            "departure_time TEXT",
            "stop_id INTEGER",
            "stop_sequence INTEGER",
            "pickup_type INTEGER",
            "drop_off_type INTEGER",
            "shape_dist_traveled"
        ],
        "trips": [
            "route_id INTEGER",
            "service_id INTEGER",
            "trip_id TEXT",
            "trip_headsign TEXT",
            "direction_id INTEGER",
            "shape_id INTEGER",
            "wheelchair_accessible INTEGER"
        ]
    }

    # Create tables and match them with data
    for file_name, columns in files_names.items():
        # Create table
        cursor.execute("CREATE TABLE IF NOT EXISTS " + file_name + " (" + ", ".join([var for var in columns]) + ")")

        # Parse and insert data from the matching CSV file
        parse_file(cursor, os.path.join(dir_path, file_name + ".txt"), file_name)

        # save (commit) changes after processing each file
        conn.commit()

    # Close the database connection
    conn.close()
    print("\n\n  **\t Database creation completed successfully.")


if __name__ == '__main__':
    main()