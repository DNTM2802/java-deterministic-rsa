package drsa;

// External libs for command line parsing and chart drawing
import org.apache.commons.cli.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import drsa.utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This application implements the Pseudo-random Byte Generator (drsa.PRBG).<br>
 * It may do one of two things:<br>
 * - Perform benchmarking of the drsa.PRBG setup - uses random passwords and multiple confusion strings and
 *   iteration counters to test the setup of the generator, and produces a histogram with the processing
 *   time per confusion string size [1-4], for each iteration count {1, 5, 10, 20, 50, 100, 200}.
 *   For that, only the flag -bmk or -benchmark is needed. It may take a long time.<br>
 * - Output nob (number of bytes) or infinite number of pseudo-random bytes to stdout - using the given password,
 *   confusion string and iteration count. For that, the flags -pwd, -cs, -ic, and -nob are needed. If nob {@literal <} 1,
 *   the program will output an infinite number of bytes.
 */
public class randgen {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        // Generate bytes option
        Options outputOptions = new Options();

        Option opt_pwd = Option.builder("pwd")
                .argName("Password")
                .required(true)
                .longOpt("password")
                .desc("Password (textual)")
                .hasArg()
                .build();

        Option opt_cs = Option.builder("cs")
                .argName("Confusion String")
                .required(true)
                .longOpt("confusion_string")
                .desc("Confusion string (textual)")
                .hasArg()
                .build();

        Option opt_ic = Option.builder("ic")
                .argName("Iteration Count")
                .required(true)
                .longOpt("iteration_count")
                .desc("Iteration count (number)")
                .hasArg()
                .build();

        Option opt_nob = Option.builder("nob")
                .argName("Number of bytes")
                .required(true)
                .longOpt("number_of_bytes")
                .desc("Number of bytes to output (number)")
                .hasArg()
                .build();

        outputOptions.addOption(opt_pwd);
        outputOptions.addOption(opt_cs);
        outputOptions.addOption(opt_ic);
        outputOptions.addOption(opt_nob);

        // Benchmark option
        Options benchmarkOptions = new Options();
        Option opt_bmk = Option.builder("bmk")
                .argName("Benchmark")
                .longOpt("benchmark")
                .desc("Perform benchmarking with random parameters")
                .build();
        benchmarkOptions.addOption(opt_bmk);

        // Parse options
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmds = parser.parse(benchmarkOptions, args, true);
            if (cmds.getOptions().length == 0) {

                // Output options selected ?
                cmds = parser.parse(outputOptions, args);

                String pwd = cmds.getOptionValue("pwd");
                String cs = cmds.getOptionValue("cs");
                int ic = Integer.parseInt(cmds.getOptionValue("ic"));
                int nob = Integer.parseInt(cmds.getOptionValue("nob"));

                PRBG prbg = new PRBG(pwd, cs, ic);
                prbg.setup();

                if (nob >=1 ) {
                    for (int i = 0; i < nob; i++) {
                        long b = prbg.next_byte();
                        if (b >= 128)
                            b = b - 256;
                        System.out.write(new byte[] {Byte.parseByte(String.valueOf(b))});
                    }
                }
                else {
                    while (true) {
                        long b = prbg.next_byte();
                        if (b >= 128)
                            b = b - 256;
                        System.out.write(new byte[] {Byte.parseByte(String.valueOf(b))});
                    }
                }

            } else {
                // Benchmark options selected

                // Times file
                SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                Date creation_date = new Date(System.currentTimeMillis());
                BufferedWriter writer = new BufferedWriter(new FileWriter(String.format("times-%s.txt", formatter.format(creation_date))));

                // Reusable variables
                long start;
                float total;
                PRBG prbg;
                int cs_size;
                XYSeries series_icX;
                String pwd;
                String cs;

                // Conditions
                int min_cs_size = 1;
                int max_cs_size = 4;
                int[] ic_counts = {1, 5, 10, 20, 50, 100, 200};

                // Time elapsed for each confusion string size, comparing multiple iteration counts

                // Collection of icX series
                XYSeriesCollection t_per_cs_icX = new XYSeriesCollection();

                for (int ic_count : ic_counts) {

                    // Series for this iteration count
                    series_icX = new XYSeries(String.format("ic=%d", ic_count));

                    for (cs_size = min_cs_size; cs_size <= max_cs_size; cs_size++) {

                        // Generator init
                        pwd = Utils.randomString(10);
                        cs = Utils.randomString(cs_size);
                        prbg = new PRBG(pwd, cs, ic_count);

                        // Count setup time
                        start = System.currentTimeMillis();
                        prbg.setup();
                        total = (System.currentTimeMillis() - start) / 1000F;

                        // Save data for later plot
                        series_icX.add(cs_size, total);

                        // Save time to times file
                        writer.write(String.format("cs_size=%d\tic=%d\ttime=%f\n", cs_size, ic_count, total));
                    }

                    // Save this icX series to Collection
                    t_per_cs_icX.addSeries(series_icX);

                }

                // Close times file
                Date end_date = new Date(System.currentTimeMillis());
                writer.write(String.format("\n\nFinished. %s", formatter.format(end_date)));
                writer.close();

                // Make and save scatter plot
                JFreeChart scatterPlot = ChartFactory.createScatterPlot(
                        "Time per confusion string size, for each iteration count",
                        "Confusion string size",
                        "Time (seconds)",
                        t_per_cs_icX
                );
                ChartUtils.saveChartAsPNG(new File("t_per_cs_icX.png"), scatterPlot, 600, 400);

            }
        } catch (ParseException e) {
            System.err.println("Error parsing command line options");
            System.err.println(e.getMessage());
            System.exit(1);
        }

    }
}