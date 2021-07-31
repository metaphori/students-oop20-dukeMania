package AudioEngine;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class KeyboardSynth implements Synth{

    private class Note {
        public Note(float freq, long time, WaveTable[]waves, Function<Long, Float> noteLFO, Function<Long, Float> volumeLFO, double [] offsets) {
            double[] steps = Arrays.stream(offsets).map(x->((Settings.WAVETABLE_SIZE * (x * freq)) / Settings.SAMPLE_RATE)).toArray();
            final double[] positions = new double[steps.length];
            long total = (long) (time * Settings.SAMPLESPERMILLI + env.getTime() + 100);
            this.buff = LongStream.range(0, total).mapToDouble(
                k-> {
                    float noteLfoVal = noteLFO.apply(k);
                    return 	IntStream.range(0, steps.length)
                            .mapToDouble(x->waves[x].getAt((int) ((positions[x] = positions[x] + steps[x] * noteLfoVal)  % Settings.WAVETABLE_SIZE)))
                            .sum() / steps.length * volumeLFO.apply(k);
                }
            ).toArray();
        }

        private int processedSamples = 0;
        private EnveloperIterator<Float> envIterator = env.createEnveloper();
        private final double[] buff;

        public float nextSample() {
            this.processedSamples = (this.processedSamples + 1) % this.buff.length;
            return (float) this.buff[this.processedSamples] * envIterator.next();
        }

        public void playMillis(long ttl){
            this.envIterator.refresh(ttl);
        }
    }


    // TODO CAVARE ACTIVE USARE HASNEXT
    private final Map<Float, Note> keys = new HashMap<>();
    private final Set<Float> active = new HashSet<>();
    private final Enveloper env;

    /**
     * costructor of KeyboardSynth, usually called by a builder
     * @param freqs the frequencies of the notes we want to load
     */
    public KeyboardSynth(Enveloper env, WaveTable[]waves, Function<Long, Float> nlfo, Function<Long, Float> vlfo, double [] offsets, List<Pair<Float, Long>> freqs){
        this.env = env;
        freqs.forEach(x->keys.put(x.getX(), new Note(x.getX(), x.getY(), waves, nlfo, vlfo, offsets)));
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int checkKeys() {
        this.active.removeIf(x->!keys.get(x).envIterator.hasNext());
        return this.active.size();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public float getSample() {
        return (float) active.stream().mapToDouble(x->keys.get(x).nextSample()).sum();
    }
    /**
     * Given a certain frequency, play that note for a certain amount of time
     * @param freq the frequency of the note that wants to be played
     * @param micros how many microseconds we want the note to be played
     */
    public void playTimedNote(float freq, Long micros) {
        active.add(freq);
        keys.get(freq).playMillis(micros / 1000);
    }
}
