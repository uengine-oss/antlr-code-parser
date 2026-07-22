typedef struct Sample {
    int value;
} Sample;

int sample_add(Sample *sample, int number) {
    return sample->value + number;
}
