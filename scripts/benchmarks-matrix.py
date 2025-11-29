import subprocess
import statistics
import numpy as np
import matplotlib.pyplot as plt
import os

# ===================================================================
# CONFIGURACIÓN DEL EXPERIMENTO
# ===================================================================

N_RUNS = 50   # cantidad de ejecuciones para promediar

EXECUTOR_THREADS = [1, 2, 4, 8, 12, 16, 20]
FORKJOIN_THREADS = [1, 2, 4, 8, 12, 16, 20]
FORKJOIN_THRESHOLDS = [16, 32, 64, 128]
VIRTUAL_THREADS = [1, 2, 4, 8, 16, 32, 64, 256, 512, 1024]

SEQ_CMD = "/home/jmatilla/tp-concurrencia/scripts/run_matrix_sequential.sh"
EXECUTOR_CMD = "/home/jmatilla/tp-concurrencia/scripts/run_matrix_executor.sh"
FORKJOIN_CMD = "/home/jmatilla/tp-concurrencia/scripts/run_matrix_forkjoin.sh"
VIRTUAL_CMD = "/home/jmatilla/tp-concurrencia/scripts/run_matrix_virtual.sh"

os.makedirs("./plots", exist_ok=True)


# ===================================================================
# AUXILIAR
# ===================================================================
def run_and_get_time(cmd):
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    for line in result.stdout.splitlines():
        if "Tiempo (ms)" in line:
            return float(line.split(":")[1].strip())
    raise RuntimeError("No se encontró el tiempo.")


# ===================================================================
# BENCHMARKS
# ===================================================================
def benchmark_executor():
    results = {}
    for n in EXECUTOR_THREADS:
        times = [run_and_get_time(f"{EXECUTOR_CMD} {n}") for _ in range(N_RUNS)]
        results[n] = {"mean": statistics.mean(times), "std": statistics.stdev(times)}
    return results


def benchmark_forkjoin():
    results = {}
    for n in FORKJOIN_THREADS:
        for th in FORKJOIN_THRESHOLDS:
            times = [run_and_get_time(f"{FORKJOIN_CMD} {n} {th}") for _ in range(N_RUNS)]
            results[(n, th)] = {"mean": statistics.mean(times), "std": statistics.stdev(times)}
    return results


def benchmark_virtual():
    results = {}
    for n in VIRTUAL_THREADS:
        times = [run_and_get_time(f"{VIRTUAL_CMD} {n}") for _ in range(N_RUNS)]
        results[n] = {"mean": statistics.mean(times), "std": statistics.stdev(times)}
    return results


def benchmark_sequential():
    times = [run_and_get_time(SEQ_CMD) for _ in range(N_RUNS)]
    return {"mean": statistics.mean(times), "std": statistics.stdev(times)}


# ===================================================================
# GRÁFICOS
# ===================================================================
def configure_integer_x_axis(ax, values):
    ax.set_xticks(values)
    ax.set_xticklabels([str(v) for v in values])


def plot_executor(results):
    threads = sorted(results.keys())
    means = [results[t]["mean"] for t in threads]
    stds = [results[t]["std"] for t in threads]

    fig, ax = plt.subplots()
    ax.errorbar(threads, means, yerr=stds, fmt='o-', capsize=5)

    configure_integer_x_axis(ax, threads)

    ax.set_title("Tiempo vs Threads — ExecutorService")
    ax.set_xlabel("Threads")
    ax.set_ylabel("Tiempo (ms)")
    ax.grid(True)
    fig.savefig("./plots/executor_times.png")


def plot_forkjoin(results):
    fig, ax = plt.subplots()

    for th in FORKJOIN_THRESHOLDS:
        xs = []
        means = []
        stds = []
        for n in FORKJOIN_THREADS:
            xs.append(n)
            means.append(results[(n, th)]["mean"])
            stds.append(results[(n, th)]["std"])

        ax.errorbar(xs, means, yerr=stds, fmt='o-', capsize=5, label=f"threshold={th}")

    configure_integer_x_axis(ax, FORKJOIN_THREADS)

    ax.set_title("ForkJoin — Tiempo vs Threads según threshold")
    ax.set_xlabel("Threads")
    ax.set_ylabel("Tiempo (ms)")
    ax.grid(True)
    ax.legend()
    fig.savefig("./plots/forkjoin_times.png")


def plot_virtual(results):
    threads = sorted(results.keys())
    means = [results[t]["mean"] for t in threads]
    stds = [results[t]["std"] for t in threads]

    fig, ax = plt.subplots()

    ax.errorbar(threads, means, yerr=stds, fmt='o-', capsize=5)

    ax.set_xscale("log", base=2)

    # Mostrar los valores de virtual threads como labels enteros
    ax.set_xticks(threads)
    ax.set_xticklabels([str(t) for t in threads])

    ax.set_title("Tiempo vs Virtual Threads")
    ax.set_xlabel("Virtual Threads (escala log2)")
    ax.set_ylabel("Tiempo (ms)")
    ax.grid(True, which="both", axis="both")

    fig.savefig("./plots/virtual_times.png")


def plot_summary(seq, exec_res, fj_res, virt_res):
    best_exec = min(exec_res.items(), key=lambda x: x[1]["mean"])
    best_fj = min(fj_res.items(), key=lambda x: x[1]["mean"])
    best_virt = min(virt_res.items(), key=lambda x: x[1]["mean"])

    labels = [
        "Secuencial",
        f"Executor ({best_exec[0]})",
        f"ForkJoin ({best_fj[0][0]}, th={best_fj[0][1]})",
        f"Virtual ({best_virt[0]})"
    ]
    values = [
        seq["mean"],
        best_exec[1]["mean"],
        best_fj[1]["mean"],
        best_virt[1]["mean"]
    ]
    stds = [
        seq["std"],
        best_exec[1]["std"],
        best_fj[1]["std"],
        best_virt[1]["std"]
    ]

    fig, ax = plt.subplots()
    ax.bar(labels, values, yerr=stds, capsize=5)
    ax.set_ylabel("Tiempo (ms)")
    ax.set_title("Comparación final — mejores configuraciones")
    ax.grid(True, axis='y')
    fig.savefig("./plots/summary_comparison.png")


# ===================================================================
# MAIN
# ===================================================================
if __name__ == "__main__":
    print("Midiendo versión secuencial...")
    seq = benchmark_sequential()

    print("Midiendo ExecutorService...")
    exec_res = benchmark_executor()

    print("Midiendo ForkJoin...")
    fj_res = benchmark_forkjoin()

    print("Midiendo Virtual Threads...")
    virt_res = benchmark_virtual()

    print("Generando gráficos...")
    plot_executor(exec_res)
    plot_forkjoin(fj_res)
    plot_virtual(virt_res)
    plot_summary(seq, exec_res, fj_res, virt_res)

    print("Gráficos generados en ./plots/")

