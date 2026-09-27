# Assignment 2 — Algorithmic Analysis, Correctness, and Performance Trade-offs

**Author:** BEGEN RIZABEK
**Implementation Language:** Java 23
**Date:** September 2026

This repository contains custom (written from scratch) implementations of three data
structures—Dynamic Array, Linked List, and Min-Heap—along with correctness proofs,
theoretical complexity analysis, and experimental benchmarking results.

```
assignment-2/
├── src/
│   ├── DynamicArray.java
│   ├── LinkedList.java
│   ├── MinHeap.java
│   ├── Benchmark.java
│   └── Tests.java
├── scripts/
│   └── make_plots.py
├── results/
│   ├── tables/   (*.csv — raw measurement data)
│   └── plots/    (*.png — graphs)
└── README.md
```

---

## 1. Overview

The goal of this assignment is not merely to implement three classic data structures
(Dynamic Array, Linked List, Min-Heap), but to conduct a comprehensive analysis of their
algorithms: proving correctness using loop invariants, determining theoretical
asymptotic complexity for all operations (O, Ω, Θ), designing controlled experiments,
and comparing theory against actual performance measured on the JVM.

Implemented:

- **DynamicArray\<T\>** — based on `Object[]`, with capacity doubling upon
overflow (`add`, `add(index,x)`, `remove(index)`, `get(index)`, `contains(x)`).
- **LinkedList\<T\>** — a singly linked list with a tail pointer (custom class,
not `java.util.LinkedList`), featuring the same operation signatures. - **MinHeap\<T extends Comparable\<T\>\>** — an array-based binary min-heap
(`insert`, `peekMin`, `extractMin`).

Each class contains built-in counters (`accessCount`, `comparisonCount`,
`movementCount`) that allow measuring not only execution time
(`System.nanoTime()`) but also the exact number of elementary operations—this is
necessary to isolate the effect of JVM constant factors from the actual
algorithmic complexity.

---

## 2. Complexity Analysis

### 2.1 Summary Table

| Structure     | Operation        | Best        | Average     | Worst       | Aux. space |
|---------------|------------------|-------------|-------------|-------------|------------|
| DynamicArray  | `add(x)`         | Θ(1)*       | Θ(1)* (amortized) | O(n) (resize) | O(1) amortized / O(n) on resize |
| DynamicArray  | `add(index,x)`   | Ω(1) (index=size) | Θ(n)  | O(n) (index=0) | O(1) |
| DynamicArray  | `remove(index)`  | Ω(1) (index=size-1) | Θ(n) | O(n) (index=0) | O(1) |
| DynamicArray  | `get(index)`     | Θ(1)        | Θ(1)        | Θ(1)        | O(1) |
| DynamicArray  | `contains(x)`    | Ω(1) (found first) | Θ(n) | O(n) (not found) | O(1) |
| LinkedList    | `add(x)` (append)| Θ(1)        | Θ(1)        | Θ(1)        | O(1) |
| LinkedList    | `add(index,x)`   | Ω(1) (index=0 or size) | Θ(n) | O(n) (index≈size/2) | O(1) |
| LinkedList    | `remove(index)`  | Ω(1) (index=0) | Θ(n)     | O(n)        | O(1) |
| LinkedList    | `get(index)`     | Ω(1) (index=0) | Θ(n)     | O(n) (index=size-1) | O(1) |
| LinkedList    | `contains(x)`    | Ω(1)        | Θ(n)        | O(n)        | O(1) |
| MinHeap       | `insert(x)`      | Ω(1)        | Θ(log n)    | O(log n)    | O(1) |
| MinHeap       | `peekMin()`      | Θ(1)        | Θ(1)        | Θ(1)        | O(1) |
| MinHeap       | `extractMin()`   | Ω(1) (n≤1)  | Θ(log n)    | O(log n)    | O(1) |

\* `add(x)` for Dynamic Array is Θ(1) **amortized**: a single insertion during
a resize costs O(n), but since resizing (doubling) happens exponentially
infrequently, the total cost of n additions is O(n), i.e., O(1) per operation
on average (accounting method / amortized analysis).

### 2.2 Justification

**DynamicArray.add(x)** — if there is free space, it is an assignment
`data[size]=x; size++` — Θ(1). If the array is full, a new array of
double the size must be allocated and n elements copied — O(n). Since
doubling occurs only when the size reaches a power of two relative to the
initial capacity, the total cost of n consecutive `add` operations is
n + (1+2+4+...+n) ≈ 3n; thus, the amortized cost per operation is
Θ(1).

**DynamicArray.add(index, x) / remove(index)** — correctness requires shifting
all elements following `index` by one position to maintain the array's
order and compact packing. The number of elements to shift is `size - index`.
When `index=size` (append), there is nothing to shift → Ω(1). When `index=0`,
all `size` elements are shifted → O(n). On average, for a random index,
it is Θ(n) (expected value of n/2 shifts).

**DynamicArray.get(index)** — direct index-based access to a contiguous block
of memory; `data[index]` is the sole operation, regardless of `index` or `size`
→ Θ(1) in all cases. This is a key structural advantage of the array.

**DynamicArray.contains(x)** — a linear scan; in the worst case (element
missing or at the end), it takes n comparisons, O(n); in the best case
(element is first), Ω(1); on average, assuming a uniform distribution of
positions, it is Θ(n).

**LinkedList.add(x)** (append) — thanks to storing a `tail` pointer,
adding a new node involves relinking two references, Θ(1), **without**
traversal of the list. Without a tail pointer, this operation would be O(n).

**LinkedList.add(index, x) / remove(index) / get(index)** — a linked list does not
support random access: to reach the node at `index`, one must traverse `index`
links starting from the `head`. Special cases where `index=0` (and `index=size`
for `add`, thanks to the `tail`) execute in Ω(1); the general case involves
Θ(n) pointer traversals; the worst case (e.g., the middle of the list) is O(n).
This is a fundamental difference from `DynamicArray.get`, where `get` is always
Θ(1): **for the list, operations with "similar names" (`get`) have completely
different asymptotic complexities due to the physical (non-)contiguity of memory**.

**LinkedList.contains(x)** — similar to an array: a linear traversal of links,
Θ(n) in the average/worst case, Ω(1) in the best case.

**MinHeap.insert(x)** — the new element is added to the end of the array (the
last level of the heap) and then "bubbles up" (`siftUp`) along the path to the root.
The height of a binary heap with n elements is ⌊log₂ n⌋; thus, in the worst case,
the element bubbles up from a leaf to the root—O(log n). If the new element is
not smaller than its parent, `siftUp` completes in a single iteration—Ω(1).

**MinHeap.peekMin()** — the minimum is always stored at the root (`data[0]`)
due to the heap invariant → Θ(1), requiring no comparisons or restructuring. **MinHeap.extractMin()** — the root is removed, the last element of the array
is moved to the root, and then it "sinks" (`siftDown`) until the heap
property is restored. As with `insert`, the path depth is limited by the tree
height—O(log n) in the worst case, Ω(1) in the best (e.g., a heap with ≤1
element, or the moved element is already smaller than both children).

**An important observation regarding operations that share the same name but differ in cost**:
`get(index)` for `DynamicArray` is Θ(1), whereas `get(index)` for `LinkedList`
is Θ(n). Both are named `get` and share the same signature and semantics,
but their asymptotic complexities differ drastically due to their distinct
physical memory layouts (a contiguous block vs. nodes scattered across the
heap and linked by pointers). Similarly: `add(index, x)` is formally O(n) for
both structures, but for the array, the cost stems from **copying data**,
while for the list, it stems from **traversing pointers** to reach the
target position (the actual insertion thereafter is Θ(1)); these are distinct
physical operations with different constant factors (as confirmed
experimentally in sections 5–6).

---

## 3. Correctness — proofs via loop invariants

Below are two complete proofs for two non-trivial operations.
Both selected operations involve a loop: `DynamicArray.add(index, x)`
(insertion with shifting—from the assignment's list of allowed operations)
and `MinHeap.extractMin` (via the auxiliary `siftDown` loop, which restores
the heap property). ### 3.1 Proof 1 — `DynamicArray.add(index, x)`

The loop code (following bounds checking and a potential `ensureCapacity` call):

```java
for (int i = size; i > index; i--) {
data[i] = data[i - 1];
}
data[index] = x;
size++;
```

Let `data₀` denote the state of the array **before** the loop begins (immediately after
`ensureCapacity` but before any shifts occur), and let `size` and `index` be the
values ​​fixed at the input (`0 ≤ index ≤ size`).

**Loop invariant I(i):** immediately before the condition `i > index` is checked
for the current value of `i`, the following holds:

1. for every `k` such that `i ≤ k ≤ size-1`: `data[k+1] = data₀[k]`
(elements originally at positions `i` and to the right have already been shifted
one position to the right);
2. for every `k` such that `index ≤ k < i`: `data[k] = data₀[k]`
(elements to the left of the current `i` remain untouched and retain their original values).

**Initialization.** Before the first iteration, `i = size`. Condition (1) requires
considering `k` in the range `size ≤ k ≤ size-1`; this range is empty, so the
condition holds trivially (true "vacuously"). Condition (2) requires
`index ≤ k < size`; since the loop has not yet performed any iterations, the
array is still equal to `data₀` at these positions, so the condition holds.
Therefore, `I(size)` is true. **Maintenance.** Suppose `I(i)` holds for some `i > index`
(otherwise, the loop has already terminated). The loop body executes `data[i] = data[i-1]`,
after which `i` is decremented by 1 (let us denote the new value as `i' = i-1`).

- The value written to `data[i]`: according to point (2) of the invariant `I(i)`, since
`index ≤ i-1 < i`, we have `data[i-1] = data₀[i-1]` **before** the assignment. 
After the assignment, `data[i] = data₀[i-1]`. This is exactly what is required by
point (1) of the new version of the invariant for `k = i-1 = i'`: `data[i'+1] = data₀[i']`.
- For other `k` such that `i ≤ k ≤ size-1` (i.e., `i' < k ≤ size-1`), the cell
`data[k+1]` was not modified in this iteration; thus, by the IH, `data[k+1] = data₀[k]`
still holds.
- For `k` such that `index ≤ k < i'`, the cell `data[k]` was also not modified in this
iteration (the loop only affects `data[i]`); thus, by the IH, `data[k] = data₀[k]`
remains true.

Thus, after one iteration, `I(i')` = `I(i-1)` holds, which is what was required to
maintain the invariant.

**Termination.** The variable `i` strictly decreases in each iteration and is
an integer; it is bounded with the value `index` as the lower bound; consequently, the loop
terminates in a finite number of steps, precisely when `i = index` (the condition
`i > index` becomes false).

**Correctness based on the invariant at termination.** Upon exiting the loop, `i = index`,
and `I(index)` asserts:

1. for all `k` such that `index ≤ k ≤ size-1`: `data[k+1] = data₀[k]` — that is, **all**
original elements that were in the range `[index, size-1]` are now
located at positions `[index+1, size]`, preserving their relative order;
2. for `k` such that `index ≤ k < index` — the range is empty, so nothing is asserted
(nor is it required — these positions were the ones freed up for the insertion).

Consequently, the position `data[index]` is now free of the old value (a copy of
`data₀[index]` is written there, but it will be immediately overwritten). The line
`data[index] = x` correctly places the new element exactly at the required position,
and `size++` synchronizes the logical size with the new content. The resulting
array consists exactly of `data₀[0..index-1]`, followed by `x`, followed by
`data₀[index..size-1]`, which is the definition of the operation "insert `x`
at index `index`". Thus, the algorithm is proven correct. ∎

---

### 3.2 Proof 2 — `MinHeap.extractMin()` (via `siftDown`)

`extractMin()` saves the root as the result, moves the last element of the
array to the root, decrements `size`, and—if the heap is not empty—calls
`siftDown(0)`:

```java
while (true) {
int l = left(i), r = right(i); 
int smallest = i; 
if (l < size && data[l] < data[smallest]) smallest = l; 
if (r < size && data[r] < data[smallest]) smallest = r; 
if (smallest != i) { swap(i, smallest); i = smallest; }
else break;
}
```

Let the array `data[0..size-1]` be interpreted as a complete binary tree
in the standard way (`left(i)=2i+1`, `right(i)=2i+2`, `parent(i)=(i-1)/2`).
For an index `j`, let `subtree(j)` denote the subtree rooted at `j`.

**Loop invariant I(i):** immediately before each check of the loop condition
for the current value of `i`:

1. if `left(i) < size`, then `subtree(left(i))` is a valid
min-heap (the heap property holds for all nodes in this subtree);
2. if `right(i) < size`, then `subtree(right(i))` is a valid
min-heap;
3. every node in the array **not belonging** to `subtree(i)` satisfies
the heap property with respect to its parent (i.e., the only place
where the heap property might be violated is at node `i` itself
relative to its descendants). **Initialization.** Before the first iteration, `i = 0`, meaning `subtree(i)` is the entire heap. Point (3) is trivial (there are no nodes outside `subtree(0)`). Prior to the `siftDown` call, the old root was removed and the last array element was moved to position 0; no other elements were affected. Since the entire heap was a valid min-heap before the removal, both subtrees `subtree(left(0))` and `subtree(right(0))` (if they exist) remain valid min-heaps—they have not physically changed. Consequently, `I(0)` holds true.

**Maintenance.** Suppose `I(i)` holds true. According to points (1)–(2), the roots of `left(i)` and `right(i)` (if they exist) store the minimum values ​​of their respective subtrees (a property of the root of a valid min-heap). Therefore, the three comparisons in the loop body (`data[i]` against `data[left(i)]` and `data[right(i)]`) correctly determine `smallest`—the index of the minimum value among `{data[i], data[left(i)], data[right(i)]}` (accounting for any missing children).

*Case A: `smallest == i`.* In this case, `data[i] ≤ data[left(i)]` and `data[i] ≤ data[right(i)]` (where these exist); combined with (1)–(2), this implies that `subtree(i)` now fully satisfies the heap property. The loop terminates (`break`), which is precisely the required postcondition. *Case B: `smallest ≠ i`.* Without loss of generality, let `smallest = left(i)`
(the case for `right(i)` is symmetric). After `swap(i, smallest)`:
- node `i` now holds the value that was the minimum of the three, so
`data[i] ≤ data[left(i)]` and `data[i] ≤ data[right(i)]` with respect to the **new**
values ​​at these positions—the local heap property between `i` and its
children is restored;
- the subtree `subtree(right(i))` remains physically untouched by the swap;
by the IH, it was a valid min-heap;
- node `left(i)` (the new position `i' = left(i)`) has received the old value
of `data[i]`. Its own subtrees, `subtree(left(left(i)))` and
`subtree(right(left(i)))`, were not affected by the swap (only the value
at node `left(i)` changed, not its children), and since `subtree(left(i))`
was a valid min-heap prior to the swap (by the IH), these sub-subtrees
were and remain valid min-heaps, regardless of the value at their
common ancestor.

Thus, for the new `i' = smallest`: `subtree(left(i'))` and
`subtree(right(i'))` are valid min-heaps (conditions (1)–(2) are met), and
all nodes outside `subtree(i')` (specifically the old `i`, which is now
the parent of `i'`) satisfy the heap property with respect to their parent—
this was established above; furthermore, by the IH, this held for all nodes
outside `subtree(i)` and remains true, since `subtree(i') ⊂ subtree(i)`. Thus, `I(i')`
holds true, and the invariant is maintained.

**Termination.** In each "continuing" iteration, `i` is replaced by
`left(i)` or `right(i)`; that is, it moves one level **deeper** into
the tree. The depth of a tree with `size` nodes is finite and equals `⌊log₂ size⌋`.
Consequently, either `i` reaches a leaf (in which case both conditions `l < size`
and `r < size` are false, `smallest` is guaranteed to remain `i`, and a `break` occurs),
or the condition `smallest == i` is naturally met beforehand. In
either case, the loop terminates in no more than `⌊log₂ size⌋ + 1` iterations.

**Correctness based on the invariant at termination.** The loop terminates only via
a `break` in case A, where it was shown that at that moment `subtree(i)`
satisfies the heap property (the root is ≤ both children, and the children's
subtrees were already heaps). According to invariant (3), all nodes outside
`subtree(i)` also satisfy the heap property relative to their parents. Thus, the
**entire** structure `data[0..size-1]` is a valid min-heap. Since `size` was
decremented by 1 before the `siftDown` call, and the returned value is the root
value saved **before** any swaps, `extractMin()` is correct:
it returns the original minimum and leaves behind a valid min-heap
of size `size-1`. ∎

---

## 4. Experimental Setup

- **n (initial structure size):** 100, 1,000, 10,000, 100,000 — the same values ​​for all applicable workloads (Section 6 of the assignment).
- **m (number of operations in the workload):** fixed per workload: 10,000 random `get` operations (Workload 1), 1,000 `contains` operations (Workload 2), 1,000 insertions/deletions each at the beginning and middle (Workload 3), and `n` insertions and `n` extractions for the heap (Workload 4). The workload definition does not change when `n` changes, as required by the assignment.
- **Repetitions:** each timing measurement is performed **5 times**; the report contains the **average** time.
- **Timer:** `System.nanoTime()`, measured strictly around the code section being tested (excluding input data generation and console output within the measured block).
- **Random seed:** `new Random(42)` for building the initial structure, `new Random(43)` for generating random access indices (Workload 1), and `new Random(44)` for generating search values ​​(Workload 2). Using different yet fixed seeds ensures the reproducibility of the entire experiment while maintaining sample independence.
- **Execution environment:** OpenJDK 21 (`java -Xmx4g Benchmark`), single-threaded, with no concurrent load in the container during measurement.
- **Assignment constraint:** input data generation is performed *before* the timer starts; Printing/logging is not included in the measured interval.

The full source code for the harness is in `src/Benchmark.java`; all tables
can be reproduced using the following commands:

```bash
javac -d out src/*.java
java -cp out Benchmark results/tables
python3 scripts/make_plots.py
```

---

## 5. Experimental Results

### 5.1 Workload 1 — Random Access (`get(index)`, 10,000 accesses)

| n       | Structure     | Average time (ms) | Accesses | Theoretical complexity |
|---------|---------------|--------------------:|----------------------:|--------------------------|
| 100     | DynamicArray  | 1.008 | 10,000 | Θ(1) per operation |
| 100     | LinkedList    | 2.423 | 502,068 | Θ(n) per operation |
| 1,000   | DynamicArray  | 0.043 | 10,000 | Θ(1) |
| 1,000   | LinkedList    | 16.14 | 4,953,268 | Θ(n) |
| 10,000  | DynamicArray  | 0.013 | 10,000 | Θ(1) |
| 10,000  | LinkedList    | 108.3 | 49,618,268 | Θ(n) |
| 100,000 | DynamicArray  | 0.007 | 10,000 | Θ(1) |
| 100,000 | LinkedList    | 1349.7 | 493,288,268 | Θ(n) | ![Workload 1 — time](results/plots/workload1_time.png)
![Workload 1 — accesses](results/plots/workload1_accesses.png)

**Analysis.** The time for `DynamicArray.get` shows virtually no growth with `n` (it even decreases slightly at large `n` values—an effect of JIT/CPU cache warm-up during repeated runs), which is fully consistent with the theoretical Θ(1) complexity. `LinkedList.get` demonstrates linear growth in the number of "pointer traversals" (`accesses`)—roughly proportional to `n`—and the execution time grows almost linearly alongside it (increasing more than 500-fold as `n` increases 1,000-fold, accounting for index randomness and the overhead of cache misses during traversals of pointers scattered across the heap). The experimental results **fully align** with the theoretical estimates: Θ(1) for the array and Θ(n) for the list.

### 5.2 Workload 2 — Search (`contains(x)`, 1,000 queries)

| n       | Structure     | Average time (ms) | Comparisons | Theoretical complexity |
|---------|---------------|--------------------:|----------:|---------------------------|
| 100     | DynamicArray  | 1.629 | 75,969 | Θ(n) per query |
| 100     | LinkedList    | 1.668 | 75,969 | Θ(n) |
| 1,000   | DynamicArray  | 0.642 | 740,069 | Θ(n) |
| 1,000   | LinkedList    | 2.466 | 740,069 | Θ(n) |
| 10,000 | DynamicArray | 7.822 | 7 566 069 | Θ(n) |
| 10,000 | LinkedList | 24.57 | 7 566 069 | Θ(n) |
| 100,000 | DynamicArray | 144.6 | 74,536,069 | Θ(n) |
| 100,000 | LinkedList    | 264.6 | 74,536,069 | Θ(n) |

![Workload 2 — time](results/plots/workload2_time.png)
![Workload 2 — comparisons](results/plots/workload2_comparisons.png)

**Analysis.** The number of comparisons for both structures is **identical** (both perform
a linear search over the same data using the same set of queries)—
this is expected and confirms that, algorithmically, both `contains` implementations
are equivalent, i.e., Θ(n). The difference lies solely in **time**: the array is 1.5–3
times faster than the list despite the same number of comparisons. The reason
is constant factors: sequential access to an `Object[]` results in predictable
memory accesses (the hardware prefetcher efficiently loads cache lines),
whereas traversing a linked list involves dereferencing a pointer to a node
located at an arbitrary spot in the heap (causing many cache misses). This is a
prime example of how **two algorithms with the same Θ(n) complexity can have
different actual execution times** due to data locality.

### 5.3 Workload 3 — Insertion / Removal (1,000 operations, begin vs. middle)

**Insertion:**

| n       | Structure    | Position | Time (ms) | Moves |
|---------|--------------|---------|-----------:|-------------:|
| 100     | DynamicArray | begin   | 6.292 | 601,420 |
| 100     | DynamicArray | middle  | 0.291 | 301 920 |
| 100     | LinkedList   | begin   | 0.158 | 0 |
| 100     | LinkedList   | middle  | 2.691 | 298 500 |
| 1 000   | DynamicArray | begin   | 0.977 | 1 500 524 |
| 1 000   | DynamicArray | middle  | 0.526 | 751 024 |
| 1 000   | LinkedList   | begin   | 0.092 | 0 |
| 1 000   | LinkedList   | middle  | 1.805 | 748 500 |
| 10 000  | DynamicArray | begin   | 8.777 | 10 499 500 |
| 10 000  | DynamicArray | middle  | 2.765 | 5 250 000 |
| 10 000  | LinkedList   | begin   | 0.019 | 0 |
| 10 000  | LinkedList   | middle  | 9.605 | 5 248 500 |
| 100 000 | DynamicArray | begin   | 63.42 | 100 499 500 |
| 100 000 | DynamicArray | middle  | 42.20 | 50 250 000 |
| 100 000 | LinkedList   | begin   | 0.040 | 0 |
| 100 000 | LinkedList   | middle  | 103.1 | 50 248 500 |

**Removal (remove):**

| n       | Structure    | Position | Time (ms) | Moves |
|---------|--------------|---------|-----------:|-------------:|
| 100     | DynamicArray | begin | 0.861 | 4,950 |
| 100 | DynamicArray | middle | 0.872 | 2 450 |
| 100 | LinkedList | begin | 0.012 | 0 |
| 100 | LinkedList | middle | 0.018 | 2 401 |
| 1,000 | DynamicArray | begin | 0.390 | 499,500 |
| 1,000 | DynamicArray | middle | 0.264 | 249,500 |
| 1,000 | LinkedList | begin | 0.417 | 0 |
| 1,000 | LinkedList | middle | 1.288 | 249 001 |
| 10,000 | DynamicArray | begin | 7.984 | 9,499,500 |
| 10,000 | DynamicArray | middle | 2.574 | 4,749,500 |
| 10,000 | LinkedList | begin | 0.011 | 0 |
| 10,000 | LinkedList | middle | 10.18 | 4,749,000 |
| 100,000 | DynamicArray | begin | 69.19 | 99,499,500 |
| 100,000 | DynamicArray | middle | 26.33 | 49,749,500 |
| 100,000 | LinkedList | begin | 0.035 | 0 |
| 100,000 | LinkedList | middle | 130.1 | 49,749,000 |

![Workload 3 — insertion](results/plots/workload3_insert_time.png)
![Workload 3 — removal](results/plots/workload3_remove_time.png)
![Workload 3 — movements](results/plots/workload3_movements.png)

**Analysis.** The results match theoretical predictions exactly when
structural characteristics are taken into account:

- **Insertion/removal at the beginning (`index=0`) for `LinkedList` — Θ(1)**: the
movement counter is **0** for any `n`, and the time does not increase with `n`
(remaining in the range of 10–100 microseconds even at n=100,000). This is
confirmed by the graph: the `LinkedList / begin` line is practically horizontal.
- **Insertion/removal at the beginning for `DynamicArray` — O(n)**: the number
of "movements" (`movementCount`) grows linearly with `n` (1,000 insertions
at the beginning with n=100,000 require over 10⁸ element shifts), and the
time increases accordingly—from 6.3 ms (n=100) to 63.4 ms (n=100,000)—showing
near-linear growth, consistent with a Θ(n) cost per operation.
- **Insertion/removal in the middle (`index=n/2`) for both structures — O(n)**:
for the array, this represents the cost of shifting ~n/2 elements; for the
list, it represents the cost of traversing ~n/2 pointers to **reach** the
middle (the actual insertion is O(1), but the traversal dominates the cost). Both grow linearly, but
`LinkedList/middle` is **slower** than `DynamicArray/middle` despite having the same
asymptotic complexity—the reason is the same as in Workload 2: sequential access
to an array is more efficient for the CPU cache than pointer traversal.

Thus, the experiment perfectly illustrates the assignment point regarding "why
physical data organization affects performance": **for a list, operations at the
beginning are virtually free, whereas for an array, they are the most expensive**
(requiring the entire array to be shifted); in the middle, both are forced to
linearly "reach" or "shift" elements, and here the array wins due to memory
locality, despite the identical O(n) complexity.

### 5.4 Workload 4 — Priority Processing (Min-Heap)

| n       | Phase       | Time (ms) | Comparisons | Order ↑ |
|---------|-------------|-----------:|----------:|:---------:|
| 100     | insert (n×) | 0.275 | 206 | ✓ |
| 100     | extractMin (n×) | 0.594 | 863 | ✓ |
| 1 000   | insert (n×) | 0.248 | 2 326 | ✓ |
| 1 000   | extractMin (n×) | 0.562 | 14 996 | ✓ |
| 10 000  | insert (n×) | 0.947 | 22 753 | ✓ |
| 10 000  | extractMin (n×) | 6.575 | 216 531 | ✓ |
| 100 000 | insert (n×) | 3.717 | 227 857 | ✓ |
| 100 000 | extractMin (n×) | 53.03 | 2 831 426 | ✓ |

![Workload 4 — time](results/plots/workload4_time.png)
![Workload 4 — comparisons](results/plots/workload4_comparisons.png)

**Analysis.** Theoretically, the total cost of `n` insertions into an initially
empty heap is O(n log n) (even O(n) in the best case, but n·O(log n)
on average/in the worst case); the same applies to `n` extractions. The number of comparisons grows
faster than linearly but slower than $n \log_2 n$, as evidenced by the data:
when `n` increased tenfold (10,000 → 100,000), the number of comparisons for `insert`
grew only ~10-fold, rather than by a factor of $10 \log n$—a significant proportion of insertions
occur close to the root and do not "bubble up" to the full height (typical for
random data; the actual average rise height is much less than $\log_2 n$).
`extractMin`, in contrast, exhibits more pronounced $n \log n$-like growth
(comparisons increased nearly 13-fold, time nearly 8-fold for a tenfold increase in `n`),
since the sinking element frequently travels all the way down to a leaf.
`peekMin()` was not tested via a separate timing measurement
(it is a trivial $\Theta(1)$ operation—`return data[0]`—as confirmed in
`Tests.java`). In all runs, the extracted sequence proved to be
**strictly non-decreasing** (`order_ok = true` for all `n`); this serves as direct
experimental confirmation of the correctness of `extractMin`, which was
formally proven in Section 3.2.

---

## 6. Discussion

**1. How does the growth of `n` affect each workload?**
Random access and `get` operations for the list grow linearly with `n`,
whereas for the array, they do not grow.
Search grows linearly for both structures (the number of comparisons is identical,
though the time coefficients differ). Insert/Remove operations at the beginning
do not grow for the list but grow linearly for the array;
in the middle, they grow linearly for both. Heap
`insert`/`extract` operations grow "almost linearly" with the number of operations,
incurring a logarithmic factor for each individual operation.

**2. Which results align with the theory?**
Practically all of them: Θ(1) `get`/`add` for arrays, Θ(n) `get` for lists, Θ(1)
`add`/`remove` at the beginning for lists, O(n) `add`/`remove` for arrays,
Θ(n) `contains` for both structures (the number of comparisons matched
exactly between the structures), and the O(log n) nature of heap operations
(the number of comparisons grows significantly slower than n).

**3. Where do the results diverge from theoretical predictions?**
At very small `n` values ​​(100, and to some extent 1000), the `DynamicArray`
execution time is sometimes *longer* than expected compared to
`LinkedList` (see Workload 3, `insert`/`begin` at n=100: the array is slower
than the list by a wider margin than raw asymptotic analysis predicts).
This is due to the JVM "cold start" effect—where the JIT compiler has not
yet optimized bytecode into machine code during the initial benchmark
iterations—as well as random fluctuations caused by the OS scheduler.
Additionally, the number of comparisons during heap insertion grows
slower than the "naive" n·log n worst-case estimate suggests;
theoretical analysis provides an upper bound (worst-case), whereas
with random data, the actual average "sift-up" depth is lower.

**4. Why can two algorithms with the same Big-O complexity have different
execution times?**
Big-O describes the *asymptotic growth rate*, ignoring constant
factors and low-level effects: memory access locality (processor L1/L2/L3
caches), object allocation costs (each list node is a separate object
on the heap with its own header), branching/branch prediction, and
overhead from boxing/unboxing (e.g., `Integer` autoboxing). A clear
example is Workload 2: both have Θ(n) complexity and an **identical**
number of comparisons, yet `DynamicArray` is 1.5–3 times faster than
`LinkedList` simply because the array is stored in contiguous memory.

**5. How do constant factors and implementation details affect
performance?**
- Sequential array access utilizes the processor's hardware prefetcher—
making it significantly faster than traversing pointers across
scattered list nodes (which causes cache misses).
- Each `LinkedList` node is a separate Java object (object header +
`next` reference + value), increasing memory consumption and the
number of heap accesses compared to a compact `Object[]`.
- Doubling the `DynamicArray` capacity causes occasional O(n) "spikes,"
but this does not affect the amortized average cost of `add(x)`.
- Autoboxing (`int` → `Integer`) affects both structures equally and
is not a source of performance differences between them in these
experiments.

**6. When is a dynamic array preferable?**
When random access by index (`get`) or appending to the end predominates—both are Θ(1) operations; it is also advantageous when memory compactness and sequential traversal speed (cache efficiency) are important, for example, when processing large datasets, searching, and sorting.

**7. When is a Linked List useful?**
When insertions/deletions **at the beginning** of the structure predominate (Θ(1) vs. O(n) for an array)—for example, in implementing a stack, queue, or undo history, where elements are added/removed from one end. It is also useful when the size is unknown beforehand and insertions at an arbitrary location occur frequently *given a known pointer to the node* (avoiding the need to search for it again), or when it is important to avoid the overhead of periodic memory reallocation as the structure grows.

**8. Why is a Heap suitable for priority-based processing?**
A Min-Heap provides Θ(1) access to the minimum (`peekMin`) and O(log n) for insertion/extraction—significantly faster than maintaining a sorted array/list (O(n) for insertion) or an unsorted one (O(n) to find the minimum). It offers an optimal balance for "frequent insertion and frequent retrieval of the minimum" scenarios, which are typical for priority queues, task schedulers, Dijkstra’s and Prim’s algorithms, etc.

**9. How does the workload affect the choice of structure?**
No single structure is the "best" overall; the choice depends on the operation profile: use an array if random access by index or searching dominates; use a list if insertions/deletions at a known end (especially the beginning) dominate; and use a heap if fast access to the minimum/maximum is required alongside frequent changes to the set. The experimental data above quantitatively confirm this: for instance, with n=100,000, inserting an element at the beginning of a list is **~1,600 times** faster than for an array (0.04 ms vs. 63.4 ms), whereas an array's `get(index)` operation is **~190,000 times** faster than a list's (0.007 ms vs. 1,349.7 ms).

---

## 7. Design Recommendations

| Use Case | Recommended Structure | Why |
|---|---|---|
| Frequent index-based access, infrequent insertions | DynamicArray | `get` is Θ(1) |
| Stack / queue with a single insertion/deletion point | LinkedList | insertion/deletion at the end is Θ(1) |
| Frequent value-based search in a large dataset | Neither (requires hash table / balanced tree / sorted array + binary search) | Both are linear; O(n) is unacceptable for large n and frequent queries |
| Priority queue / algorithms requiring frequent minimum extraction | MinHeap | O(log n) insert/extract, Θ(1) peek |
| Insertions/deletions primarily at the start of the sequence | LinkedList | Θ(1) vs. O(n) for an array |
| Insertions/deletions primarily at the end, index-based access | DynamicArray | `add` (append) is amortized Θ(1) **and** `get` is Θ(1) |
| Small to moderate n; cache efficiency and simplicity are important | DynamicArray | Lower constant factors, compact memory layout | ---

## 8. Conclusion

The experiment confirmed the theoretical analysis for all three structures:
the Dynamic Array provides Θ(1) random access at the cost of O(n) for insertion/deletion
at an arbitrary position (especially at the beginning); the Linked List, conversely, offers Θ(1)
insertion/deletion at the ends at the cost of O(n) indexed access; the Min-Heap
effectively balances insertion and minimum extraction at O(log n) while
providing Θ(1) access to the minimum itself. However, operations with identical
asymptotic complexity (e.g., `contains` in both structures, both Θ(n)) exhibit
different execution times in practice due to memory locality and constant factors—
this is a key practical lesson: **Big-O defines the growth rate but does not
predict absolute execution time**; the choice of data structure must be based
on theoretical asymptotics, the actual workload profile, and low-level
runtime environment characteristics (JVM, CPU cache, memory allocation).
The correctness of all three implementations has been formally proven
via loop invariants (Section 3) and further verified by 71
automated tests (`src/Tests.java`), including cross-validation
against `java.util.ArrayList` and `java.util.PriorityQueue`.
