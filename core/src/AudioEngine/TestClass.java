package AudioEngine;
import static junit.framework.TestCase.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class TestClass {

    private boolean checkTolerance(final double actual, final double exact) {
        double tolerance = 1e-4;
        return Math.abs(actual - exact) < tolerance;
    }

    @Test
    void drumSynthTest() {
        DrumSynth ds = new DrumSynth();

        // test if the synth registers the play drum input properly
        ds.playPercussion(DrumSamples.Snare);
        ds.playPercussion(DrumSamples.Hat);
        Assertions.assertEquals(2, ds.checkKeys());

        // since we start from 0, the sum of the first <Settings.ATTENUATION> samples should be 0.
        // those samples are used to attenuate the note and bring it from the actual volume, where it got the input to be replayed, to 0
        Assertions.assertEquals(0.0, IntStream.range(0, Settings.ATTENUATION).mapToDouble(x -> ds.getSample()).sum());
        // after the attenuation samples, the value to put in the buffer has to be > 0
        Assertions.assertTrue(ds.getSample() > 0.0);

        // wait 1000000 samples, after those all the drums should have stopped playing and checkKeys has to return 0
        LongStream.range(0, 1000000).forEach(x -> ds.getSample());
        Assertions.assertEquals(0, ds.checkKeys());

        ds.playPercussion(DrumSamples.Kick);
        IntStream.range(0, 1000).forEach(x -> ds.getSample());
        ds.playPercussion(DrumSamples.Kick);
        Assertions.assertTrue(IntStream.range(0, Settings.ATTENUATION - 1).mapToDouble(x -> ds.getSample()).sum() != 0);
        Assertions.assertTrue(checkTolerance(ds.getSample(), 0.0));
        Assertions.assertTrue(ds.getSample() > 0.0);
    }

    @Test
    void testBuilder() {
        SynthBuilderImpl builder = new SynthBuilderImpl();

        builder.setWavetables(new WaveTable[]{WaveTable.Saw});
        builder.setOffsets(new double[]{1.0, 2.0});
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            builder.build(new ArrayList<>());
        });
        String expectedMessage = "offsets, enveloper or wavetables are null";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));

        builder.setEnveloper(new Enveloper(100l, 1f, 100l));
        exception = Assertions.assertThrows(Exception.class, () -> {
            builder.build(new ArrayList<>());
        });
        expectedMessage = "wavetables and offsets do not match";
        actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));

        builder.setWavetables(new WaveTable[]{WaveTable.Saw, WaveTable.Square});
        Assertions.assertDoesNotThrow(() -> builder.build(new ArrayList<>()));
    }

    @Test
    void testKeyboardSynth() throws Exception {
        SynthBuilderImpl builder = new SynthBuilderImpl();
        Enveloper env = new Enveloper(100l, 0.8f, 100l);
        builder.setEnveloper(env);
        builder.setWavetables(new WaveTable[]{WaveTable.Saw});
        builder.setOffsets(new double[]{1});
        List<Pair<Float, Long>> notes = new ArrayList<>();
        notes.add(new Pair(100f, 10000l));
        notes.add(new Pair(200f, 100000l));
        KeyboardSynth ks = builder.build(notes);

        Assertions.assertEquals(0, ks.checkKeys());
        ks.playTimedNote(100, 1000000l);
        Assertions.assertEquals(1, ks.checkKeys());
        Assertions.assertEquals(0, IntStream.range(0, Settings.ATTENUATION).mapToDouble(x -> ks.getSample()).sum());
        Assertions.assertTrue(ks.getSample() != 0);

        List<Double> allValues = IntStream.range(0, 300000).mapToDouble(x -> ks.getSample()).boxed().collect(Collectors.toList());
        double max = allValues.stream().mapToDouble(Double::doubleValue).max().getAsDouble();
        double min = allValues.stream().mapToDouble(Double::doubleValue).min().getAsDouble();
        Assertions.assertTrue(max <= 0.8f && min >= -0.8f);

        IntStream.range(0, 10000).mapToDouble(x -> ks.getSample());
        Assertions.assertEquals(0, ks.checkKeys());
    }

    @Test
    void testLFO() {
        Function<Long, Float> lfo = LFOFactory.straightLineLFO(1.5f, 500);
        long totalSamples = (long) (500 * Settings.SAMPLESPERMILLI);
        final double [] arrStraight = LongStream.range(0, totalSamples).mapToDouble(lfo::apply).toArray();
        Assertions.assertTrue(IntStream.range(0, arrStraight.length - 1).noneMatch(i -> arrStraight[i] > arrStraight[i + 1]));
        Assertions.assertEquals(1.0, lfo.apply(totalSamples), 0.0);
        Assertions.assertTrue(checkTolerance(lfo.apply(totalSamples - 1), 1.5f));

        lfo = LFOFactory.squareLFO(1.5f, 0.8f, 1000);
        totalSamples = (long) (1000 * Settings.SAMPLESPERMILLI);
        List<Double> arrSquare = LongStream.range(0, totalSamples).mapToDouble(lfo::apply).boxed().collect(Collectors.toList());
        Map<Double, Long> freqCounter = arrSquare.stream().collect(Collectors.groupingBy(x -> x, Collectors.counting()));
        long finalTotalSamples = totalSamples;
        Assertions.assertTrue(freqCounter.values().stream().allMatch(x -> x == finalTotalSamples / 2));

        lfo = LFOFactory.sineLFO(2f, 1.5f, 2000);
        totalSamples = (long) (2000 * Settings.SAMPLESPERMILLI);
        List<Double> arrSine = LongStream.range(0, totalSamples).mapToDouble(lfo::apply).boxed().collect(Collectors.toList());
        Assertions.assertEquals(2.0, arrSine.get(0));
        Assertions.assertEquals(1.5, arrSine.get((int) (totalSamples / 2)));
        Assertions.assertEquals(2.0, arrSine.get((int) totalSamples - 1));
        Assertions.assertTrue(arrSine.get((int) (totalSamples / 4)) > 1.5 || arrSine.get((int) (totalSamples / 4)) < 2.0);
        Assertions.assertTrue(arrSine.get((int) (totalSamples / 1.5)) < -1.5 || arrSine.get((int) (totalSamples / 1.5)) > -2.0);

        lfo = LFOFactory.buildIntervals(new float [] {1f, 0f, 1f, 0.5f}, 1000);
        totalSamples = (long) (1000 * Settings.SAMPLESPERMILLI);
        List<Double> arrIntervals = LongStream.range(0, totalSamples).mapToDouble(lfo::apply).boxed().collect(Collectors.toList());
        Assertions.assertTrue(arrIntervals.get(0) == 1f);
        Assertions.assertTrue(arrIntervals.get((int) (totalSamples / 4)) == 0f);
        Assertions.assertTrue(arrIntervals.get((int) (totalSamples / 2)) == 1f);
        Assertions.assertTrue(arrIntervals.get((int) (totalSamples - 1)) == 0.5f);
    }
}
