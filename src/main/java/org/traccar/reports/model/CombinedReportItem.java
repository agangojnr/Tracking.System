
package org.traccar.reports.model;

import org.traccar.model.Event;
import org.traccar.model.Position;

import java.util.List;

public class CombinedReportItem {

    private long deviceId;

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    private List<double[]> route;

    public List<double[]> getRoute() {
        return route;
    }

    public void setRoute(List<double[]> route) {
        this.route = route;
    }

    private List<Event> events;

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    private List<Position> positions;

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }

}
