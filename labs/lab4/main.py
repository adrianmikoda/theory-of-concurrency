import pandas as pd
import matplotlib.pyplot as plt

if __name__ == "__main__":
    df = pd.read_csv("results.csv")

    metrics = ["avg_producer_wait_time", "avg_consumer_wait_time", "throughput"]
    titles = ["Średni czas oczekiwania producentów [s]", "Średni czas oczekiwania konsumentów [s]", "Przepustowość"]
    limits = {m: (df[m].min(), df[m].max()) for m in metrics}

    for M, g in df.groupby("M"):
        fig, axes = plt.subplots(1, 3, figsize=(18, 5))
        for i, (ax, metric) in enumerate(zip(axes, metrics)):
            p = g.pivot(index="m", columns="n", values=metric)
            vmin, vmax = limits[metric]
            im = ax.imshow(p.values, origin="lower", cmap="viridis", vmin=vmin, vmax=vmax)

            ax.set_xticks(range(len(p.columns)), p.columns)
            ax.set_yticks(range(len(p.index)), p.index)
            ax.set_xlabel("n")
            ax.set_ylabel("m")
            ax.set_title(titles[i])
            fig.colorbar(im, ax=ax)

        fig.suptitle(f"M = {M}")
        fig.tight_layout()
        fig.savefig(f"heatmap_M{M}.png")

    plt.show()