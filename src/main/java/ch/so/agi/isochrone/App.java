package ch.so.agi.isochrone;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.locationtech.jts.algorithm.hull.ConcaveHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.isochrone.algorithm.ShortestPathTree;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.Subnetwork;
import com.graphhopper.routing.ev.VehicleAccess;
import com.graphhopper.routing.ev.VehicleSpeed;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.DefaultSnapFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.routing.weighting.custom.CustomModelParser;
import com.graphhopper.storage.index.Snap;

public class App {

    public static void main(String[] args) throws IOException {
        GraphHopper hopper = createGraphHopperInstance("/Users/stefan/Downloads/switzerland-padded.osm.pbf");
        // get encoder from GraphHopper instance
        EncodingManager encodingManager = hopper.getEncodingManager();
        BooleanEncodedValue accessEnc = encodingManager.getBooleanEncodedValue(VehicleAccess.key("car"));
        DecimalEncodedValue speedEnc = encodingManager.getDecimalEncodedValue(VehicleSpeed.key("car"));
        
        System.out.println(accessEnc);
        System.out.println(speedEnc);
        
        // snap some GPS coordinates to the routing graph and build a query graph
        Weighting weighting = CustomModelParser.createFastestWeighting(accessEnc, speedEnc, encodingManager);
        Snap snap = hopper.getLocationIndex().findClosest(47.21440, 7.51647, new DefaultSnapFilter(weighting, encodingManager.getBooleanEncodedValue(Subnetwork.key("car"))));
        QueryGraph queryGraph = QueryGraph.create(hopper.getBaseGraph(), snap);

        ShortestPathTree tree = new ShortestPathTree(queryGraph, weighting, false, TraversalMode.NODE_BASED);
        // find all nodes that are within a radius of 120s
        tree.setTimeLimit(10 * 60 * 1000);

//        MultiPoint mp = new GeometryFactory().createMultiPoint();
        
        List<Coordinate> coordList = new ArrayList<>();
        
        AtomicInteger counter = new AtomicInteger(0);
        // you need to specify a callback to define what should be done
        tree.search(snap.getClosestNode(), label -> {
            // see IsoLabel.java for more properties
//            System.out.println("node: " + label.node + ", time: " + label.time + ", distance: " + label.distance);
            
            double lat = queryGraph.getNodeAccess().getLat(label.node);
            double lon = queryGraph.getNodeAccess().getLon(label.node);
            
            double[] xy = ApproxSwissProj.WGS84toLV95(lat, lon, 450);
            
//            Point p = new GeometryFactory().createPoint(new Coordinate(lat, lon));
//            System.out.println(p);
//
            Coordinate c = new Coordinate(xy[0], xy[1]);
            coordList.add(c);
            
//            System.out.println("lat: " + lat + ", lon: " + lon);
            
            
            counter.incrementAndGet();
            
            




        });

        GeoJsonWriter gjw = new GeoJsonWriter();
        gjw.setEncodeCRS(true);

        
        Coordinate[] coords = coordList.toArray(new Coordinate[0]);
        MultiPoint mp = new GeometryFactory().createMultiPoint(coords);
        mp.setSRID(2056);
        //BufferedWriter writer1 = new BufferedWriter(new FileWriter("/Users/stefan/Downloads/erreichbarkeit_points4.json"));
        //gjw.write(mp, writer1);

        
        
        Geometry g = ConcaveHull.concaveHullByLengthRatio(mp, 0.02);
        //Geometry g = ConcaveHull.concaveHullByLength(mp, 0.001);
        g.setSRID(2056);
        
        BufferedWriter writer2 = new BufferedWriter(new FileWriter("/Users/stefan/Downloads/erreichbarkeit_polygon7.json"));
        gjw.write(g, writer2);
        
        System.out.println("Hallo Welt.");
    }
    
    
    static GraphHopper createGraphHopperInstance(String ghLoc) {
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(ghLoc);
        hopper.setGraphHopperLocation("target/isochrone-graph-cache");
        hopper.setProfiles(new Profile("car").setVehicle("car").setTurnCosts(true));
        hopper.importOrLoad();
        return hopper;
    }
}
