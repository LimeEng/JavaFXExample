package core;

import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;
import java.time.LocalDateTime;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.util.Duration;

public class ExampleChart {

	private static final int DELAY = 100;
	private static final int MAX_ITEMS = 100;

	// To generate some interesting and actual data
	private static final OperatingSystemMXBean os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

	public static XYChart<String, Number> createChart() {

		// Setup both axes, assign labels and a range to the number axis.
		CategoryAxis xAxis = new CategoryAxis();
		xAxis.setLabel("Time");
		NumberAxis yAxis = new NumberAxis(0, 100, 10);
		yAxis.setLabel("Processor load (%)");

		XYChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
		chart.setTitle("System and Java process cpu load");

		Series<String, Number> processSeries = createSeries("Processor load (Process)");
		Series<String, Number> systemSeries = createSeries("Processor load (System)");

		chart.getData()
				.addAll(processSeries, systemSeries);

		// Creating a timeline which executes parallell to the main UI thread.
		// Reads the cpu load and adds data to the chart periodically. Also
		// makes sure that there never are more than MAX_ITEMS data-points in a
		// given series.
		Timeline timeLine = new Timeline();
		timeLine.getKeyFrames()
				.add(new KeyFrame(Duration.millis(DELAY), (e) -> {
					String time = LocalDateTime.now()
							.toString()
							.split("T")[1];
					Data<String, Number> processData = new Data<>(time, os.getProcessCpuLoad() * 100);
					Data<String, Number> systemData = new Data<>(time, os.getSystemCpuLoad() * 100);

					processSeries.getData()
							.add(processData);
					systemSeries.getData()
							.add(systemData);

					chart.getData()
							.forEach(series -> {
								if (series.getData()
										.size() > MAX_ITEMS) {
									series.getData()
											.remove(0);
								}
							});
				}));
		timeLine.setCycleCount(Animation.INDEFINITE);
		timeLine.play();

		chart.setAnimated(false);

		return chart;
	}

	private static Series<String, Number> createSeries(String name) {
		Series<String, Number> series = new Series<>();
		series.setName(name);
		return series;
	}
}
