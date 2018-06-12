package regression;

public interface ISampleSource {
    /**
     * Returns random sample.
     * @return random sample
     */
    public ISample getRandomSample();

    /**
     * Returns iterable that goes sequentially through all samples.
     * @return sample sequence
     */
    public Iterable<ISample> getAllSamples();
}
