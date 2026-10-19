import numpy as np
import matplotlib.pyplot as plt


if __name__ == "__main__":
    values_without_synchronization = []
    values_semaphore_if = []
    values_semaphore_while = []

    with open("race_without_synchronization.txt") as f:
        lines_without_synchronization = f.readlines()
        values_without_synchronization = [int(line.strip()) for line in lines_without_synchronization]
        d = np.array(values_without_synchronization)
        print("Without synchronization")
        print(len(d), (d == 0).sum(), d.min(), d.max(), d.mean(), np.median(d))
    
    with open("race_semaphore_if.txt") as f:
        lines_semaphore_if = f.readlines()
        values_semaphore_if = [int(line.strip()) for line in lines_semaphore_if]
        d = np.array(values_semaphore_if)
        print("Semaphore with if")
        print(len(d), (d == 0).sum(), d.min(), d.max(), d.mean(), np.median(d))

    with open("race_semaphore_while.txt") as f:
        lines_semaphore_while = f.readlines() 
        values_semaphore_while = [int(line.strip()) for line in lines_semaphore_while]
        d = np.array(values_semaphore_while)
        print("Semaphore with while")
        print(len(d), (d == 0).sum(), d.min(), d.max(), d.mean(), np.median(d))

    lo, hi = -10000, 10000

    fig, ax = plt.subplots(1, 1, figsize=(12, 6), layout="constrained")
    counts, edges = np.histogram(values_without_synchronization, bins=100, range=(lo, hi))
    ax.stairs(counts, edges - np.diff(edges)[0] / 2, fill=True)
    ax.set_title("Histogram końcowych wartości zmiennej\nCounter z zadania 2. dla semafora z implementacja if")
    ax.set_ylabel("Liczbą wystąpień końcowych wartości zmiennej Counter")
    ax.set_xlim(lo, hi)
    ax.set_xticks(np.linspace(lo, hi, 11))
    ax.set_box_aspect(1)
    plt.savefig("histogram_without_synchronization.png", dpi=300)


    fig, axes = plt.subplots(1, 2, figsize=(12, 6), layout="constrained")

    ax = axes[0]
    counts, edges = np.histogram(values_semaphore_if, bins=100, range=(lo, hi))
    ax.stairs(counts, edges - np.diff(edges)[0] / 2, fill=True)
    ax.set_title("Histogram końcowych wartości zmiennej\nCounter z zadania 2. dla semafora z implementacja if")
    ax.set_ylabel("Liczbą wystąpień końcowych wartości zmiennej Counter")
    ax.set_xlim(lo, hi)
    ax.set_xticks(np.linspace(lo, hi, 11))
    ax.set_box_aspect(1)

    ax = axes[1]
    counts, edges = np.histogram(values_semaphore_while, bins=100, range=(lo, hi))
    ax.stairs(counts, edges - np.diff(edges)[0] / 2, fill=True)
    ax.set_title("Histogram końcowych wartości zmiennej\nCounter z zadania 2. dla semafora z implementacja while")
    ax.set_ylabel("Liczbą wystąpień końcowych wartości zmiennej Counter")
    ax.set_xlim(lo, hi)
    ax.set_xticks(np.linspace(lo, hi, 3))
    ax.set_box_aspect(1)

    fig.subplots_adjust(wspace=0.4)

    plt.savefig("histograms_semaphors.png", dpi=300)
    plt.show()
