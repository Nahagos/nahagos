import os
import sqlite3
import zipfile

"""
The script creates a database with the following tables:
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
        cursor.executemany(request, request_params)


def main():
    zip_path = input("Enter path - zip file named israel-public-transportation: ")
    dir_path = zip_path.replace(".zip", "\\")
    with zipfile.ZipFile(zip_path, 'r') as zip_ref:
        zip_ref.extractall(dir_path)

    # create the database file:
    # - delete the sql file if it exists
    try:
        os.remove('sql.db')
    except:
        pass

    # Connect to an SQLite database (create it after deleting the file)
    conn = sqlite3.connect('sql.db')

    cursor = conn.cursor()

    # Create tables columns names:
    files_names = {"stops": ["stop_id INTEGER not null PRIMARY KEY", "stop_code INTEGER not null", "stop_name TEXT not null", "stop_desc TEXT not null", "stop_lat REAL not null", "stop_lon REAL not null", "location_type INTEGER not null", "parent_station INTEGER", "zone_id INTEGER not null"],
                   "agency": ["agency_id INTEGER PRIMARY KEY", "agency_name TEXT", "agency_url TEXT", "agency_timezone TEXT", "agency_lang TEXT", "agency_phone TEXT", "agency_fare_url TEXT"],
                   "calendar": ["service_id INTEGER PRIMARY KEY", "sunday INTEGER", "monday INTEGER", "tuesday INTEGER", "wednesday INTEGER", "thursday INTEGER", "friday INTEGER", "saturday INTEGER", "start_date INTEGER", "end_date INTEGER"],
                   "routes": ["route_id INTEGER PRIMARY KEY", "agency_id INTEGER", "route_short_name TEXT", "route_long_name INTEGER", "route_desc TEXT", "route_type INTEGER", "route_color"],
                   "shapes": ["shape_id INTEGER", "shape_pt_lat REAL", "shape_pt_lon REAL", "shape_pt_sequence INTEGER"],
                   "stop_times": ["trip_id TEXT", "arrival_time TEXT", "departure_time TEXT", "stop_id INTEGER", "stop_sequence INTEGER", "pickup_type INTEGER", "drop_off_type INTEGER", "shape_dist_traveled"],
                   "trips": ["route_id INTEGER", "service_id INTEGER", "trip_id TEXT", "trip_headsign TEXT", "direction_id INTEGER", "shape_id INTEGER", "wheelchair_accessible INTEGER"]}

    # Parse each file:
    for file_name in files_names:
        cursor.execute(
            "CREATE TABLE IF NOT EXISTS " + file_name + " (" + ", ".join([var for var in files_names[file_name]]) + ")")
        parse_file(cursor, dir_path + file_name + ".txt", file_name)

        # save (commit) the changes
        conn.commit()

    # Save (commit) the changes
    conn.commit()

    # Close the connection
    conn.close()



if __name__ == '__main__':
    main()


