/**
 * Class responsible for putting all the calculations together for a particular (given) date.
 * A lot of this is described in Section 3 of the report.
 * Goes through each order, checks validity, calculates flight path, controls which orders are
 * best to deliver to maximise the number of orders delivered for the day.
 *
 * Thing to note: coordinates for Appleton Tower are hard coded in here.
 *
 * There are two functions for finalising the Orders which we decide to deliver (this is talked about in the report).
 * By default, it's finiliseFlightsForTheDay().
 * To see the effects of the other function, comment out line 77, and "comment in" line 78.
 */

package uk.ac.ed.inf;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class Drone {

    Date date;
    final LngLat APPLETON_TOWER_COOR = new LngLat(-3.186874, 55.944494);
    List<Order> ordersForTheDay;
    List<List<Flight>> flightsForTheDay = new ArrayList<>();
    List<Order> cancelledOrders = new ArrayList<>();
    int movesLeft;
    List<Long> ticks = new ArrayList<>();

    public Drone(Date dateGiven, String url) throws Exception {
        this.date = dateGiven;
        this.movesLeft = 2000;
        Database db = new Database();
        var urlS = "";
        if (url.endsWith("/")) {
            urlS = url.substring(0, url.length() - 1);
        } else {
            urlS = url;
        }
        urlS += "/orders";
        URL urlOrders = new URL(urlS);
        this.ordersForTheDay = db.getOrders(urlOrders, dateGiven);

        if (this.ordersForTheDay.size() == 0) {
            throw new Exception("No Orders on " + dateGiven);
        }
        System.out.println("Calculating flight paths:");
        for (var order : this.ordersForTheDay) {
            if (order.orderChecks()) {
                // start at appleton --> go to restaurant --> back to appleton:

                System.out.println("Flight " + this.ordersForTheDay.indexOf(order) + "/" + this.ordersForTheDay.size());
                long timeAtLastAccept = System.nanoTime();
                Flight flight = new Flight(APPLETON_TOWER_COOR, order.restaurantCoor);
                flight.calculatePath(url);

                Flight flightBack = new Flight(order.restaurantCoor, APPLETON_TOWER_COOR);
                flightBack.calculatePath(url);

                long tick = System.nanoTime() - timeAtLastAccept;
                this.ticks.add(tick);
                List<Flight> orderFlight = new ArrayList<>();
                orderFlight.add(flight);
                orderFlight.add(flightBack);
                this.flightsForTheDay.add(orderFlight);

            } else {
                this.cancelledOrders.add(order);

            }
        }
        for (var orderCancelled : this.cancelledOrders) {
            this.ordersForTheDay.remove(orderCancelled);
        }

        //finalise in which order we're going to do this:
        this.finiliseFlightsForTheDay();
//        this.notOrderedFinalise();

        for (var flight : this.flightsForTheDay) {
            this.movesLeft -= 2*flight.get(0).movesTaken;
        }
    }

    private void finiliseFlightsForTheDay() {
        // we want to see what's the most efficient way to deliver orders:
        int sum = 0;
        List<List<Flight>> sortedByMoves = new ArrayList<>();
        List<Order> ordersByMoves = new ArrayList<>(); // we need this in case we need to move some to cancelled
        List<Long> ticksOrdered = new ArrayList<>();

        sortedByMoves.add(this.flightsForTheDay.get(0));
        ordersByMoves.add(this.ordersForTheDay.get(0));
        ticksOrdered.add(this.ticks.get(0));


        sum += this.flightsForTheDay.get(0).get(0).movesTaken + this.flightsForTheDay.get(0).get(1).movesTaken;

        int movesByOrder = 1;
        for (int i = 1; i < this.flightsForTheDay.size(); i++) {
            for (int j = 0; j < movesByOrder; j++) {
                if (2*this.flightsForTheDay.get(i).get(0).movesTaken <= 2*sortedByMoves.get(j).get(0).movesTaken) {
                    sortedByMoves.add(j, this.flightsForTheDay.get(i));
                    ordersByMoves.add(j, this.ordersForTheDay.get(i));
                    ticksOrdered.add(j, this.ticks.get(i));
                    break;
                } else {
                    if (j == sortedByMoves.size()-1) {
                        sortedByMoves.add(this.flightsForTheDay.get(i));
                        ordersByMoves.add(this.ordersForTheDay.get(i));
                        ticksOrdered.add(this.ticks.get(i));
                    }
                }
            }
            movesByOrder ++;
            sum += 2*this.flightsForTheDay.get(i).get(0).movesTaken;
        }

        if (sum > 2000) {

            int checkSum = 0;
            int finalFlightIndex = 0;

            for (int i = 0; i < sortedByMoves.size(); i++) {
                if (checkSum + 2*sortedByMoves.get(i).get(0).movesTaken < 2000) {
                    checkSum += sortedByMoves.get(i).get(0).movesTaken + sortedByMoves.get(i).get(1).movesTaken;
                } else {
                    System.out.println("flight index: " + i + "/" + sortedByMoves.size());
                    finalFlightIndex = i;
                    break;
                }
            }
            for (int i = finalFlightIndex; i < sortedByMoves.size(); i++) {
                this.cancelledOrders.add(ordersByMoves.get(i));
                this.ordersForTheDay.remove(ordersByMoves.get(i));
                this.flightsForTheDay.remove(sortedByMoves.get(i));
                this.ticks.remove(ticksOrdered.get(i));
            }
        }

        for (var order : this.ordersForTheDay) {
            order.orderOutcome = OrderOutcome.Delivered;
        }
    }

    private void notOrderedFinalise() {
        int sum = 0;
        for (var flight : this.flightsForTheDay) {
            sum += 2*flight.get(0).movesTaken;
        }
        if (sum > 2000) {

            int checkSum = 0;
            int finalFlightIndex = 0;

            for (int i = 0; i < this.flightsForTheDay.size(); i++) {
                if (checkSum + 2*this.flightsForTheDay.get(i).get(0).movesTaken < 2000) {
                    checkSum += 2*this.flightsForTheDay.get(i).get(0).movesTaken;
                } else {
                    System.out.println("flight index: " + i + "/" + this.flightsForTheDay.size());
                    finalFlightIndex = i;
                    break;
                }
            }
            List<List<Flight>> toRemove = new ArrayList<>();
            List<Long> ticksToRemove = new ArrayList<>();
            for (int i = finalFlightIndex; i < this.flightsForTheDay.size(); i++) {
                this.cancelledOrders.add(this.ordersForTheDay.get(i));

                toRemove.add(this.flightsForTheDay.get(i));

                ticksToRemove.add(this.ticks.get(i));
            }
            for (var cancelledFlight : toRemove) {
                this.flightsForTheDay.remove(cancelledFlight);
            }
            for (var cancelledOrder : this.cancelledOrders) {
                this.ordersForTheDay.remove(cancelledOrder);
            }
            for (var cancelledTicks : ticksToRemove) {
                this.ticks.remove(cancelledTicks);
            }
        }

        for (var order : this.ordersForTheDay) {
            order.orderOutcome = OrderOutcome.Delivered;
        }
    }
}
