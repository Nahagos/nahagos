

import requests
from lxml import etree
from datetime import datetime

# Recorded At: 2025-01-22T21:57:06+02:00
# Item Identifier: 851217729
# Monitoring Ref: 19723
# Line Ref: 17786
# Direction Ref: 1
# Published Line Name: 2
# Vehicle Location: Longitude 34.77317, Latitude 31.220563
# Recorded At: 2025-01-22T21:57:06+02:00
# Item Identifier: 851217729
# Monitoring Ref: 19723
# Line Ref: 17786
# Direction Ref: 1
# Published Line Name: 2
# Vehicle Location: Longitude 34.77317, Latitude 31.220563
# Expected Arrival Time: 2025-01-22T22:25:00+02:00


class MonitoredStopVisitParser:
    def __init__(self, scope):
        self.namespaces = {'siri': 'http://www.siri.org.uk/siri'}
        self.scope      = scope

    def get_recorded_at_time(self):
        return self.scope.find(".//siri:RecordedAtTime", namespaces=self.namespaces).text

    def get_item_identifier(self):
        return self.scope.find(".//siri:ItemIdentifier", namespaces=self.namespaces).text

    def get_monitoring_ref(self, monitored_stop_visit):
        return self.scope.find(".//siri:MonitoringRef", namespaces=self.namespaces).text

    def get_line_ref(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:LineRef", namespaces=self.namespaces).text

    def get_direction_ref(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:DirectionRef", namespaces=self.namespaces).text

    def get_data_frame_ref(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:FramedVehicleJourneyRef/siri:DataFrameRef", namespaces=self.namespaces).text

    def get_dated_vehicle_journey_ref(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:FramedVehicleJourneyRef/siri:DatedVehicleJourneyRef", namespaces=self.namespaces).text

    def get_published_line_name(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:PublishedLineName", namespaces=self.namespaces).text

    def get_operator_ref(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:OperatorRef", namespaces=self.namespaces).text

    def get_destination_ref(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:DestinationRef", namespaces=self.namespaces).text

    def get_origin_aimed_departure_time(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:OriginAimedDepartureTime", namespaces=self.namespaces).text

    def get_confidence_level(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:ConfidenceLevel", namespaces=self.namespaces).text

    def get_vehicle_location(self):
        if vehicle_journey := self.scope.find(".//siri:MonitoredVehicleJourney", namespaces=namespaces):
            if vehicle_location := vehicle_journey.find(".//siri:VehicleLocation", namespaces=namespaces):
                longitude = vehicle_location.find(".//siri:Longitude", namespaces=namespaces).text
                latitude = vehicle_location.find(".//siri:Latitude", namespaces=namespaces).text
                return [longitude, latitude]
            
        return None

    def get_bearing(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:Bearing", namespaces=self.namespaces).text

    def get_velocity(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:Velocity", namespaces=self.namespaces).text

    def get_license_plate(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:VehicleRef", namespaces=self.namespaces).text

    def get_stop_point_ref(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:MonitoredCall/siri:StopPointRef", namespaces=self.namespaces).text

    def get_order(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:MonitoredCall/siri:Order", namespaces=self.namespaces).text

    def get_expected_arrival_time(self):
        end_time = datetime.fromisoformat(self.scope.find(".//siri:MonitoredVehicleJourney/siri:MonitoredCall/siri:ExpectedArrivalTime", namespaces=self.namespaces).text)
        return [end_time.year, end_time.month, end_time.day, end_time.hour, end_time.minute, end_time.tzinfo]

    def get_distance_from_stop(self):
        return self.scope.find(".//siri:MonitoredVehicleJourney/siri:MonitoredCall/siri:DistanceFromStop", namespaces=self.namespaces).text




# Iterate over each MonitoredStopVisit tag



xml_data = """<?xml version='1.0' encoding='UTF-8'?><Siri xmlns="http://www.siri.org.uk/siri"><ServiceDelivery><ResponseTimestamp>2025-01-22T22:02:01+02:00</ResponseTimestamp><ProducerRef>Moran</ProducerRef><ResponseMessageIdentifier>5d7c2011-c850-459f-a8b5-f473c2cabd1e</ResponseMessageIdentifier><RequestMessageRef>1737576121080</RequestMessageRef><Status>true</Status><StopMonitoringDelivery version="2.8"><ResponseTimestamp>2025-01-22T22:02:01+02:00</ResponseTimestamp><Status>true</Status><MonitoredStopVisit><RecordedAtTime>2025-01-22T22:01:52+02:00</RecordedAtTime><ItemIdentifier>165224958</ItemIdentifier><MonitoringRef>19723</MonitoringRef><MonitoredVehicleJourney><LineRef>17786</LineRef><DirectionRef>1</DirectionRef><FramedVehicleJourneyRef><DataFrameRef>2025-01-22</DataFrameRef><DatedVehicleJourneyRef>36184806</DatedVehicleJourneyRef></FramedVehicleJourneyRef><PublishedLineName>2</PublishedLineName><OperatorRef>32</OperatorRef><DestinationRef>11749</DestinationRef><OriginAimedDepartureTime>2025-01-22T21:35:00+02:00</OriginAimedDepartureTime><ConfidenceLevel>probablyReliable</ConfidenceLevel><VehicleLocation><Longitude>34.811691</Longitude><Latitude>31.267118</Latitude></VehicleLocation><Bearing>52</Bearing><Velocity>32</Velocity><VehicleRef>9289901</VehicleRef><MonitoredCall><StopPointRef>19723</StopPointRef><Order>40</Order><ExpectedArrivalTime>2025-01-22T22:06:00+02:00</ExpectedArrivalTime><DistanceFromStop>12696</DistanceFromStop></MonitoredCall></MonitoredVehicleJourney></MonitoredStopVisit><MonitoredStopVisit><RecordedAtTime>2025-01-22T22:01:51+02:00</RecordedAtTime><ItemIdentifier>165224959</ItemIdentifier><MonitoringRef>19723</MonitoringRef><MonitoredVehicleJourney><LineRef>17786</LineRef><DirectionRef>1</DirectionRef><FramedVehicleJourneyRef><DataFrameRef>2025-01-22</DataFrameRef><DatedVehicleJourneyRef>36184807</DatedVehicleJourneyRef></FramedVehicleJourneyRef><PublishedLineName>2</PublishedLineName><OperatorRef>32</OperatorRef><DestinationRef>11749</DestinationRef><OriginAimedDepartureTime>2025-01-22T21:55:00+02:00</OriginAimedDepartureTime><ConfidenceLevel>probablyReliable</ConfidenceLevel><VehicleLocation><Longitude>34.774326</Longitude><Latitude>31.227975</Latitude></VehicleLocation><Bearing>17</Bearing><Velocity>0</Velocity><VehicleRef>68517703</VehicleRef><MonitoredCall><StopPointRef>19723</StopPointRef><Order>40</Order><ExpectedArrivalTime>2025-01-22T22:26:00+02:00</ExpectedArrivalTime><DistanceFromStop>3117</DistanceFromStop></MonitoredCall></MonitoredVehicleJourney></MonitoredStopVisit></StopMonitoringDelivery></ServiceDelivery></Siri>"""
MOT_API_URL = "http://moran.mot.gov.il:110/Channels/HTTPChannel/SmQuery/2.8/xml?Key=LA353500&MonitoringRef="
PROXY_URL   = "http://gp.lavirz.com:8043/"

proxies = {
        "http": PROXY_URL
    }

try:
    response = requests.get(MOT_API_URL + str(21140), proxies=proxies)
    
    if response.status_code == 200:
        #print(response.text)
        root = etree.fromstring(response.text.encode("utf-8"))
        namespaces = {'siri': 'http://www.siri.org.uk/siri'}
        for monitored_stop_visit in root.xpath(".//siri:MonitoredStopVisit", namespaces=namespaces):
            #print(monitored_stop_visit.find(".//siri:RecordedAtTime", namespaces=namespaces).text)
            #print(monitored_stop_visit.find(".//siri:RecordedAtTime", namespaces=namespaces).text)

            obj = MonitoredStopVisitParser(monitored_stop_visit)
            print(obj.get_dated_vehicle_journey_ref())

    
                



    else:
        print(f"Failed to connect. Status code: {response.status_code}")

except requests.exceptions.RequestException as e:
    print("kaki")