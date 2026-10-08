import numpy as np
import matplotlib.pyplot as plt


if __name__ == "__main__":
    values_with_lock = []
    values_without_lock = []
    
    with open("race_with_lock.txt") as f:
        lines_with_lock = f.readlines()
        values_with_lock = [int(line.strip()) for line in lines_with_lock]
        d = np.array(values_with_lock)
        print("values_with_lock")
        print(len(d), (d == 0).sum(), d.min(), d.max(), d.mean(), np.median(d))

    with open("race_without_lock.txt") as f:
        lines_without_lock = f.readlines() 
        values_without_lock = [int(line.strip()) for line in lines_without_lock]
        d = np.array(values_without_lock)
        print("values_without_lock")
        print(len(d), (d == 0).sum(), d.min(), d.max(), d.mean(), np.median(d))

    lo, hi = -10000, 10000
    fig, axes = plt.subplots(1, 2, figsize=(12, 6), layout="constrained")

    ax = axes[0]
    counts, edges = np.histogram(values_without_lock, bins=100, range=(lo, hi))
    ax.stairs(counts, edges - np.diff(edges)[0] / 2, fill=True)
    ax.set_title("Histogram końcowych wartości zmiennej\nCounter z punktu 1.2")
    ax.set_ylabel("Liczbą wystąpień końcowych wartości zmiennej Counter")
    ax.set_xlim(lo, hi)
    ax.set_xticks(np.linspace(lo, hi, 11))
    ax.set_box_aspect(1)

    ax = axes[1]
    counts, edges = np.histogram(values_with_lock, bins=100, range=(lo, hi))
    ax.stairs(counts, edges - np.diff(edges)[0] / 2, fill=True)
    ax.set_title("Histogram końcowych wartości zmiennej Counter\npo wprowadzeniu mechanizmu synchronizacji z punktu 1.3")
    ax.set_ylabel("Liczbą wystąpień końcowych wartości zmiennej Counter")
    ax.set_xlim(lo, hi)
    ax.set_xticks(np.linspace(lo, hi, 3))
    ax.set_box_aspect(1)

    fig.subplots_adjust(wspace=0.4)

    plt.savefig("histograms.png", dpi=300)
    plt.show()
