#!/usr/bin/env python3

"""Compare JMH summary-table result files. First file is the baseline.

Usage: jmhcmp.py <baseline> <candidate> [<candidate> ...]

For each benchmark row, prints the baseline score alongside each candidate's
score, the percent change, and a significance marker based on JMH's reported
+/-Error (the 99.9% CI half-width): a change counts as significant only when the
two confidence intervals do not overlap. Lower avgt = faster = better.
"""

import sys, math

RESET, RED, GREEN, DIM, BOLD, CYAN = (
    "\033[0m", "\033[31m", "\033[32m", "\033[2m", "\033[1m", "\033[36m")

def parse(path):
    rows = {}
    with open(path) as fh:
        for line in fh:
            t = line.split()
            if not t or t[0] == "Benchmark" or "avgt" not in t and "thrpt" not in t:
                continue
            # Layout: name <params...> Mode Cnt Score ± Error Units
            # Find the mode token (avgt/thrpt); columns before Cnt/Score follow.
            try:
                mi = t.index("avgt") if "avgt" in t else t.index("thrpt")
            except ValueError:
                continue
            name = t[0]
            params = [p for p in t[1:mi] if p != "N/A"]
            score = float(t[mi + 2])
            err = float(t[mi + 4]) if t[mi + 3] == "±" else float("nan")
            unit = t[-1]
            key = name + (" " + "/".join(params) if params else "")
            rows[key] = (score, err, unit)
    return rows

def main():
    if len(sys.argv) < 3:
        sys.exit(__doc__)
    files = sys.argv[1:]
    data = {f: parse(f) for f in files}
    base_name, *cand_names = files
    base = data[base_name]

    keyw = max(len(k) for k in base)
    header = f"{'benchmark':<{keyw}}  {base_name[:11]:>11}"
    for c in cand_names:
        header += f" | {c[:22]:>22}"
    print(BOLD + header + RESET)
    print("-" * len(header))

    tallies = {c: {"better": 0, "worse": 0, "noise": 0, "ratios": []} for c in cand_names}

    for key in base:
        bscore, berr, bunit = base[key]
        row = f"{key:<{keyw}}  {bscore:>8.3f} {bunit:<3}"
        for c in cand_names:
            if key not in data[c]:
                row += f" | {'—':>22}"
                continue
            cscore, cerr, _ = data[c][key]
            pct = (cscore - bscore) / bscore * 100.0
            # Non-overlapping 99.9% CIs => significant.
            sig = abs(cscore - bscore) > (berr + cerr)
            tallies[c]["ratios"].append(cscore / bscore)
            if not sig:
                col, mark, bucket = DIM, "~", "noise"
            elif pct < 0:
                col, mark, bucket = GREEN, "▼", "better"
            else:
                col, mark, bucket = RED, "▲", "worse"
            tallies[c][bucket] += 1
            cell = f"{cscore:>8.3f} {mark} {pct:+6.1f}%"
            row += f" | {col}{cell:>22}{RESET}"
        print(row)

    print("-" * len(header))
    print(BOLD + "summary (vs %s; ▼=faster ▲=slower ~=within noise)" % base_name + RESET)
    for c in cand_names:
        t = tallies[c]
        # geometric mean of score ratios -> overall speed factor
        gm = math.exp(sum(math.log(r) for r in t["ratios"]) / len(t["ratios"]))
        overall = (gm - 1) * 100
        ocol = GREEN if overall < 0 else RED
        print(f"  {CYAN}{c}{RESET}: "
              f"{GREEN}{t['better']} faster{RESET}, "
              f"{RED}{t['worse']} slower{RESET}, "
              f"{DIM}{t['noise']} noise{RESET}  |  "
              f"overall {ocol}{overall:+.1f}%{RESET} (geomean)")

if __name__ == "__main__":
    main()
