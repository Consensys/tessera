package com.github.tessera.api;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.glassfish.jersey.server.monitoring.MonitoringStatistics;
import org.glassfish.jersey.server.monitoring.ResourceStatistics;
import org.glassfish.jersey.server.monitoring.TimeWindowStatistics;

@Path("/metrics")
public class MetricsResource {

    @Inject
    Provider<MonitoringStatistics> monitoringStatisticsProvider;

    @GET
    @Produces("text/plain")
    public String getSomething() {
        final MonitoringStatistics monitoringStatistics = monitoringStatisticsProvider.get();

        final TimeWindowStatistics timeWindowStatistics = monitoringStatistics.getRequestStatistics().getTimeWindowStatistics().get(0L);
        final TimeWindowStatistics sendRawExecutionStatistics = monitoringStatistics.getUriStatistics().get("/sendraw").getRequestExecutionStatistics().getTimeWindowStatistics().get(0L);
        final TimeWindowStatistics sendExecutionStatistics = monitoringStatistics.getUriStatistics().get("/send").getRequestExecutionStatistics().getTimeWindowStatistics().get(0L);

        return "total_request_count " + timeWindowStatistics.getRequestCount() + "\n" +
               "sendraw_request_count " + sendRawExecutionStatistics.getRequestCount() + "\n" +
               "send_request_count " + sendExecutionStatistics.getRequestCount() + "\n" +
               "total_avg_request_processing_ms " + timeWindowStatistics.getAverageDuration() + "\n" +
               "sendraw_request_processing_ms " + sendRawExecutionStatistics.getAverageDuration() + "\n" +
               "send_request_processing_ms " + sendExecutionStatistics.getAverageDuration();
    }
}
