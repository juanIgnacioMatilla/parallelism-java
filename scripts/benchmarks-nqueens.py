import subprocess
import statistics
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import os

# ================================================================
# CONFIGURACIÓN GENERAL
# ================================================================

N_RUNS = 10

THREAD_COUNTS = [1, 2, 4, 8, 12, 16, 20]
N_VALUES = [12, 13, 14]

HEATMAP_THREADS = [1, 2, 4, 8, 12, 16, 20]
HEATMAP_THRESHOLDS = [1, 2, 4, 8, 12]

VIRTUAL_THRESHOLDS = [1, 2, 3, 4, 5, 6, 7, 8]

EXECUTOR_CMD = "./scripts/run_nqueens_executor.sh"
FORKJOIN_CMD = "./scripts/run_nqueens_forkjoin.sh"
VIRTUAL_CMD = "./scripts/run_nqueens_virtual.sh"
SEQ_CMD = "./scripts/run_nqueens_sequential.sh"

os.makedirs("./plots_nqueens", exist_ok=True)


# ================================================================
# AUXILIAR
# ================================================================
def run_and_get_time(cmd):
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    for line in result.stdout.splitlines():
        if "Tiempo (ms)" in line:
            return float(line.split(":")[1].strip())
    raise RuntimeError("No se encontró 'Tiempo (ms)' en la salida: " + result.stdout)


def run_time_and_vt(cmd):
    """Devuelve (tiempo_ms, virtual_threads_creados)."""
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)

    tiempo = None
    vt = None

    for line in result.stdout.splitlines():
        if "Tiempo (ms)" in line:
            tiempo = float(line.split(":")[1].strip())
        elif "Virtual threads realmente creados" in line:
            vt = int(line.split(":")[1].strip())

    if tiempo is None or vt is None:
        raise RuntimeError("No se encontraron datos en el output:\n" + result.stdout)

    return tiempo, vt


# ================================================================
# BENCHMARKS
# ================================================================
def bench_executor(nqueen_size):
    print(f"\n[ExecutorService] N={nqueen_size}")
    results = {}
    for th in THREAD_COUNTS:
        print(f"  -> Threads={th}... ", end="", flush=True)
        times = []
        for _ in range(N_RUNS):
            t = run_and_get_time(f"{EXECUTOR_CMD} {th} {nqueen_size}")
            times.append(t)
            print(".", end="", flush=True)
        print(" OK")
        results[th] = {"mean": statistics.mean(times), "std": statistics.stdev(times)}
    return results


def bench_forkjoin(nqueen_size, threshold=None):
    if threshold is None:
        print(f"\n[ForkJoin] N={nqueen_size}")
        results = {}
        for th in THREAD_COUNTS:
            print(f"  -> Threads={th}... ", end="", flush=True)
            times = []
            for _ in range(N_RUNS):
                t = run_and_get_time(f"{FORKJOIN_CMD} {th} {nqueen_size}")
                times.append(t)
                print(".", end="", flush=True)
            print(" OK")
            results[th] = {"mean": statistics.mean(times), "std": statistics.stdev(times)}
        return results

    else:
        print(f"\n[ForkJoin Heatmap] N={nqueen_size}")
        results = {}
        for t in HEATMAP_THREADS:
            for th in HEATMAP_THRESHOLDS:
                print(f"  -> Threads={t}, threshold={th}... ", end="", flush=True)
                times = []
                for _ in range(N_RUNS):
                    val = run_and_get_time(f"{FORKJOIN_CMD} {t} {nqueen_size} {th}")
                    times.append(val)
                    print(".", end="", flush=True)
                print(" OK")
                results[(t, th)] = statistics.mean(times)
        return results


def bench_virtual(nqueen_size):
    print(f"\n[VirtualThreads] N={nqueen_size}")
    results = {}

    for th in VIRTUAL_THRESHOLDS:
        print(f"  -> threshold={th}... ", end="", flush=True)

        times = []
        vtc = []

        for _ in range(N_RUNS):
            t, vt = run_time_and_vt(f"{VIRTUAL_CMD} {nqueen_size} {th}")
            times.append(t)
            vtc.append(vt)
            print(".", end="", flush=True)

        print(" OK")

        results[th] = {
            "mean": statistics.mean(times),
            "std": statistics.stdev(times),
            "vt_mean": statistics.mean(vtc),
            "vt_std": statistics.stdev(vtc)
        }

    return results


def bench_sequential(nqueen_size):
    print(f"\n[Secuencial] N={nqueen_size}... ", end="")
    times = [run_and_get_time(f"{SEQ_CMD} {nqueen_size}") for _ in range(N_RUNS)]
    print("OK")
    return statistics.mean(times)


# ================================================================
# GRÁFICOS
# ================================================================
def plot_executor(results_by_n):
    plt.figure()
    for N, results in results_by_n.items():
        xs = sorted(results.keys())
        ys = [results[t]["mean"] for t in xs]
        stds = [results[t]["std"] for t in xs]

        plt.errorbar(xs, ys, yerr=stds,
                     fmt="o-", capsize=4, label=f"N={N}")

    plt.xticks(xs)
    plt.xlabel("Threads")
    plt.ylabel("Tiempo (ms)")
    plt.title("ExecutorService — Tiempo vs Threads")
    plt.legend()
    plt.grid(True)
    plt.savefig("./plots_nqueens/executor_vs_threads.png")


def plot_forkjoin(results_by_n):
    plt.figure()
    for N, results in results_by_n.items():
        xs = sorted(results.keys())
        ys = [results[t]["mean"] for t in xs]
        stds = [results[t]["std"] for t in xs]

        plt.errorbar(xs, ys, yerr=stds,
                     fmt="o-", capsize=4, label=f"N={N}")

    plt.xticks(xs)
    plt.xlabel("Threads")
    plt.ylabel("Tiempo (ms)")
    plt.title("ForkJoin — Tiempo vs Threads")
    plt.legend()
    plt.grid(True)
    plt.savefig("./plots_nqueens/forkjoin_vs_threads.png")


def plot_virtual(results_by_n):
    print("\n=== Conversión threshold → virtual threads creados ===")

    plt.figure(figsize=(11, 6))

    for N, results in results_by_n.items():
        thresholds = sorted(results.keys())
        vt_values = [results[th]["vt_mean"] for th in thresholds]
        times = [results[th]["mean"] for th in thresholds]
        stds = [results[th]["std"] for th in thresholds]

        # Print conversion table for this N
        print(f"\nN={N}:")
        for th in thresholds:
            print(f"  th={th} -> VT creados promedio={int(results[th]['vt_mean'])}")

        plt.errorbar(
            vt_values,
            times,
            yerr=stds,
            fmt="o-",
            capsize=4,
            label=f"N={N}"
        )

    # Escala logaritmica en eje X
    plt.xscale("log")

    # Mostrar SOLO ticks de 10^1, 10^2, 10^3, etc.
    import numpy as np
    log_min = 1
    log_max = 7   # hasta 10^7 por si threshold grande
    ticks = [10 ** i for i in range(log_min, log_max + 1)]
    plt.xticks(ticks, [f"$10^{i}$" for i in range(log_min, log_max + 1)])

    plt.xlabel("Virtual Threads creados (log)")
    plt.ylabel("Tiempo (ms)")
    plt.title("Virtual Threads — Tiempo vs Virtual Threads creados (log scale)")
    plt.grid(True, which="both", ls="--", lw=0.5)
    plt.legend()
    plt.tight_layout()

    plt.savefig("./plots_nqueens/virtual_vt_vs_time_log.png")
    plt.close()


def plot_heatmap(results, title, filename, threads, thresholds):
    data = np.zeros((len(threads), len(thresholds)))
    for i, t in enumerate(threads):
        for j, th in enumerate(thresholds):
            data[i, j] = results[(t, th)]

    # Crear formato string sin notación científica
    formatted = np.empty_like(data, dtype=object)
    for i in range(data.shape[0]):
        for j in range(data.shape[1]):
            formatted[i, j] = f"{data[i, j]:.0f}"   # sin decimales, sin e+03

    plt.figure(figsize=(8, 6))

    sns.heatmap(
        data,
        annot=formatted,          # anotaciones string
        fmt="",                   # ¡IMPORTANTE! evita que seaborn aplique formato numérico
        cmap="viridis",
        xticklabels=thresholds,
        yticklabels=threads
    )

    plt.xlabel("Threshold")
    plt.ylabel("Threads")
    plt.title(title)
    plt.savefig(f"./plots_nqueens/{filename}")
    plt.close()


def plot_final_comparison(seq_time, best_exec, best_fj, best_virt):
    plt.figure()
    labels = ["Secuencial", "Executor", "ForkJoin", "Virtual"]
    values = [seq_time, best_exec, best_fj, best_virt]

    plt.bar(labels, values)
    plt.ylabel("Tiempo (ms)")
    plt.title("Comparación final — mejores configuraciones")
    plt.grid(True, axis="y")
    plt.savefig("./plots_nqueens/final_comparison.png")


# ================================================================
# MAIN
# ================================================================
if __name__ == "__main__":

    executor_results = {N: bench_executor(N) for N in N_VALUES}
    plot_executor(executor_results)

    forkjoin_results = {N: bench_forkjoin(N) for N in N_VALUES}
    plot_forkjoin(forkjoin_results)

    virtual_results = {N: bench_virtual(N) for N in N_VALUES}
    plot_virtual(virtual_results)

    # Heatmap FORKJOIN: ahora con N=14
    fj_heat = bench_forkjoin(14, threshold=True)
    plot_heatmap(
        fj_heat,
        "ForkJoin — Heatmap tiempo (N=14)",
        "forkjoin_heatmap.png",
        HEATMAP_THREADS,
        HEATMAP_THRESHOLDS
    )

    seq = bench_sequential(14)
    best_exec = min(v["mean"] for v in executor_results[14].values())
    best_fj = min(v["mean"] for v in forkjoin_results[14].values())
    best_virt = min(v["mean"] for v in virtual_results[14].values())

    plot_final_comparison(seq, best_exec, best_fj, best_virt)

    print("\nListo. Gráficos guardados en plots_nqueens/\n")
