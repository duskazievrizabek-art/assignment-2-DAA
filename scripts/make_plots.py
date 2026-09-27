import csv
import os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

TABLES = "results/tables"
PLOTS = "results/plots"
os.makedirs(PLOTS, exist_ok=True)


def read_csv(name):
    rows = []
    with open(os.path.join(TABLES, name)) as f:
        reader = csv.DictReader(f)
        for row in reader:
            rows.append(row)
    return rows


def to_num(x):
    return float(x)


# ---------------------------------------------------------------
# Workload 1 - Random Access
# ---------------------------------------------------------------
rows = read_csv("workload1_random_access.csv")
ns = sorted(set(int(r["n"]) for r in rows))

fig, ax = plt.subplots(figsize=(7, 5))
for struct in ["DynamicArray", "LinkedList"]:
    y = [to_num(r["avg_time_ns"]) / 1e6 for r in rows if r["structure"] == struct]
    ax.plot(ns, y, marker="o", label=struct)
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n (initial size)")
ax.set_ylabel("Average time for 10,000 get(index) calls (ms)")
ax.set_title("Workload 1: Random Access - Execution Time vs n")
ax.legend()
ax.grid(True, which="both", alpha=0.3)
fig.tight_layout()
fig.savefig(os.path.join(PLOTS, "workload1_time.png"), dpi=150)
plt.close(fig)

fig, ax = plt.subplots(figsize=(7, 5))
for struct in ["DynamicArray", "LinkedList"]:
    y = [to_num(r["accesses"]) for r in rows if r["structure"] == struct]
    ax.plot(ns, y, marker="o", label=struct)
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n (initial size)")
ax.set_ylabel("Total element accesses (node hops for LinkedList)")
ax.set_title("Workload 1: Random Access - Accesses vs n")
ax.legend()
ax.grid(True, which="both", alpha=0.3)
fig.tight_layout()
fig.savefig(os.path.join(PLOTS, "workload1_accesses.png"), dpi=150)
plt.close(fig)

# ---------------------------------------------------------------
# Workload 2 - Search
# ---------------------------------------------------------------
rows = read_csv("workload2_search.csv")

fig, ax = plt.subplots(figsize=(7, 5))
for struct in ["DynamicArray", "LinkedList"]:
    y = [to_num(r["avg_time_ns"]) / 1e6 for r in rows if r["structure"] == struct]
    ax.plot(ns, y, marker="o", label=struct)
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n (initial size)")
ax.set_ylabel("Average time for 1,000 contains(x) calls (ms)")
ax.set_title("Workload 2: Search - Execution Time vs n")
ax.legend()
ax.grid(True, which="both", alpha=0.3)
fig.tight_layout()
fig.savefig(os.path.join(PLOTS, "workload2_time.png"), dpi=150)
plt.close(fig)

fig, ax = plt.subplots(figsize=(7, 5))
for struct in ["DynamicArray", "LinkedList"]:
    y = [to_num(r["comparisons"]) for r in rows if r["structure"] == struct]
    ax.plot(ns, y, marker="o", label=struct)
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n (initial size)")
ax.set_ylabel("Total comparisons (1,000 searches)")
ax.set_title("Workload 2: Search - Comparisons vs n")
ax.legend()
ax.grid(True, which="both", alpha=0.3)
fig.tight_layout()
fig.savefig(os.path.join(PLOTS, "workload2_comparisons.png"), dpi=150)
plt.close(fig)

# ---------------------------------------------------------------
# Workload 3 - Insertion / Removal
# ---------------------------------------------------------------
rows = read_csv("workload3_insert_remove.csv")

fig, ax = plt.subplots(figsize=(8, 6))
combos = [("DynamicArray", "insert", "begin"), ("DynamicArray", "insert", "middle"),
          ("LinkedList", "insert", "begin"), ("LinkedList", "insert", "middle")]
for struct, op, pos in combos:
    y = [to_num(r["avg_time_ns"]) / 1e6 for r in rows
         if r["structure"] == struct and r["operation"] == op and r["position"] == pos]
    ax.plot(ns, y, marker="o", label=f"{struct} / {pos}")
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n (initial size)")
ax.set_ylabel("Average time for 1,000 insertions (ms)")
ax.set_title("Workload 3: Insertion Time vs n (begin vs middle)")
ax.legend()
ax.grid(True, which="both", alpha=0.3)
fig.tight_layout()
fig.savefig(os.path.join(PLOTS, "workload3_insert_time.png"), dpi=150)
plt.close(fig)

fig, ax = plt.subplots(figsize=(8, 6))
combos = [("DynamicArray", "remove", "begin"), ("DynamicArray", "remove", "middle"),
          ("LinkedList", "remove", "begin"), ("LinkedList", "remove", "middle")]
for struct, op, pos in combos:
    y = [to_num(r["avg_time_ns"]) / 1e6 for r in rows
         if r["structure"] == struct and r["operation"] == op and r["position"] == pos]
    ax.plot(ns, y, marker="o", label=f"{struct} / {pos}")
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n (initial size)")
ax.set_ylabel("Average time for 1,000 removals (ms)")
ax.set_title("Workload 3: Removal Time vs n (begin vs middle)")
ax.legend()
ax.grid(True, which="both", alpha=0.3)
fig.tight_layout()
fig.savefig(os.path.join(PLOTS, "workload3_remove_time.png"), dpi=150)
plt.close(fig)

fig, ax = plt.subplots(figsize=(8, 6))
combos = [("DynamicArray", "insert", "begin"), ("LinkedList", "insert", "begin"),
          ("DynamicArray", "insert", "middle"), ("LinkedList", "insert", "middle")]
for struct, op, pos in combos:
    y = [to_num(r["movements"]) for r in rows
         if r["structure"] == struct and r["operation"] == op and r["position"] == pos]
    ax.plot(ns, y, marker="o", label=f"{struct} / {pos}")
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n (initial size)")
ax.set_ylabel("Element movements / pointer hops (1,000 insertions)")
ax.set_title("Workload 3: Insertion Movements vs n")
ax.legend()
ax.grid(True, which="both", alpha=0.3)
fig.tight_layout()
fig.savefig(os.path.join(PLOTS, "workload3_movements.png"), dpi=150)
plt.close(fig)

# ---------------------------------------------------------------
# Workload 4 - Min-Heap
# ---------------------------------------------------------------
rows = read_csv("workload4_heap.csv")

fig, ax = plt.subplots(figsize=(7, 5))
for phase in ["insert", "extractMin"]:
    y = [to_num(r["avg_time_ns"]) / 1e6 for r in rows if r["phase"] == phase]
    ax.plot(ns, y, marker="o", label=phase)
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n")
ax.set_ylabel("Total time for n operations (ms)")
ax.set_title("Workload 4: Min-Heap - Execution Time vs n")
ax.legend()
ax.grid(True, which="both", alpha=0.3)
fig.tight_layout()
fig.savefig(os.path.join(PLOTS, "workload4_time.png"), dpi=150)
plt.close(fig)

fig, ax = plt.subplots(figsize=(7, 5))
for phase in ["insert", "extractMin"]:
    y = [to_num(r["comparisons"]) for r in rows if r["phase"] == phase]
    ax.plot(ns, y, marker="o", label=phase)
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n")
ax.set_ylabel("Total comparisons")
ax.set_title("Workload 4: Min-Heap - Comparisons vs n")
ax.legend()
ax.grid(True, which="both", alpha=0.3)
fig.tight_layout()
fig.savefig(os.path.join(PLOTS, "workload4_comparisons.png"), dpi=150)
plt.close(fig)

print("All plots written to", PLOTS)
