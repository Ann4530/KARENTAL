---
name: evaluate-findgap
description: "Evaluate ket qua cua skill findgap: nhan log JSON + bang ket qua he thong, tinh TP/FP/FN, Precision/Recall/F1, va dien vao test_findgap_result_theflash.csv."
---

# Evaluate FindGap — Danh Gia Ket Qua Gap-Finding

> Output bang **tieng Viet**. Chi giu tieng Anh cho ten file, code block, ten metric.

## Muc dich

Skill nay nhan ket qua tu he thong gap-finding (log JSON + bang gap classifications), tinh cac metric danh gia (Precision, Recall, F1), va dien vao file CSV ket qua test.

---

## Arguments

```
<uc_id> <csv_path>
```

- `<uc_id>` = ID cua use case vua chay, vd `UC-06`
- `<csv_path>` = duong dan den file CSV ket qua, mac dinh `test_findgap_result_theflash.csv`

User se paste truc tiep vao chat:
1. **Log JSON** tu he thong (chua tokens, cost, duration) — neu khong co thi ghi `—` cho token/latency/cost
2. **Bang ket qua** (cac dong co cot Tag: match/mismatch/missing/surplus)
3. **Expected Output** = tong hop ky vong (vd: "2 match, 1 mismatch, 1 missing, 1 surplus")

---

## WORKFLOW

### Buoc 1: Parse Input

Tu **log JSON**, trich xuat:
| Field | JSON path |
|-------|-----------|
| UC ID | `uc_id` |
| PR title | `pr_title` |
| Duration | `total.duration_s` |
| Input tokens | `total.input_tokens` |
| Output tokens | `total.output_tokens` |
| Cost | `total.cost_usd` |

Tu **bang ket qua**, dem theo tag:
```
match_actual    = so dong tag "match"
mismatch_actual = so dong tag "mismatch"
missing_actual  = so dong tag "missing"
surplus_actual  = so dong tag "surplus"
```

Tu **Expected Output**, dem:
```
match_exp      = so match ky vong
mismatch_exp   = so mismatch ky vong
missing_exp    = so missing ky vong
surplus_exp    = so surplus ky vong
```

---

### Buoc 2: Tinh Metrics

**Dinh nghia "gaps"** = cac item khong phai match (mismatch + missing + surplus).

```
expected_gaps = mismatch_exp + missing_exp + surplus_exp
actual_gaps   = mismatch_actual + missing_actual + surplus_actual
```

**Tinh TP theo tung loai**:
```
TP_mismatch = min(mismatch_exp, mismatch_actual)
TP_missing  = min(missing_exp,  missing_actual)
TP_surplus  = min(surplus_exp,  surplus_actual)
TP          = TP_mismatch + TP_missing + TP_surplus

FP = actual_gaps - TP
FN = expected_gaps - TP
```

**Tinh ty le**:
```
Precision = TP / (TP + FP)
Recall    = TP / (TP + FN)
F1        = 2 x Precision x Recall / (Precision + Recall)
```

**Danh gia F1**:
| F1 | Muc |
|----|-----|
| < 0.5 | Kem |
| 0.5 – 0.7 | Chap nhan duoc |
| 0.7 – 0.85 | Tot |
| > 0.85 | Xuat sac (>0.95 check overfitting) |

---

### Buoc 3: Phan tich Gap Categories

Xac dinh nhan tong:
- `Match` — tat ca deu match, khong co gap nao
- `Missing` — chu yeu missing (AI bo sot nhieu buoc FE/BE)
- `Mismatch` — chu yeu mismatch (AI nham loai gap)
- `Surplus` — chu yeu surplus (AI tao ra nhieu gap khong co trong expected)
- `Mixed` — nhieu loai gap ket hop

---

### Buoc 4: Dien CSV

File CSV su dung DUNG cac cot sau (giu nguyen ten cot va thu tu cot):

```
STT,Use Case,Gap Categories,Expected Gap,Actual Gap,Token,Recall,Precision,F1 Score,Latency (s),Security,Stability,Cost (USD),Expected Output,Actual Output,Logs
```

**Format tung cot:**

| Cot | Format | Vi du |
|-----|--------|-------|
| STT | so thu tu | `2` |
| Use Case | `{uc_id}: {pr_title}` | `UC-02: View Challenge Detail` |
| Gap Categories | nhan tu Buoc 3 | `Mixed` |
| Expected Gap | multi-line list, dong dau la tong so gaps | `"6 gaps\n- MISMATCH-01: ..."` |
| Actual Gap | 1 paragraph co danh so `(1) (2) (3)...` | `"(1) Phat hien dung..."` |
| Token | tong `input_tokens + output_tokens`, co dau phay hang nghin | `39,630` |
| Recall | `{decimal_3} ({percent_rounded}%)` | `0.833 (83%)` |
| Precision | `{decimal_3} ({percent_rounded}%)` | `0.500 (50%)` |
| F1 Score | so thap phan 2 chu so | `0.63` |
| Latency (s) | so giay thap phan 2 chu so | `132.83` |
| Security | `N/A` neu khong co danh gia dac biet | `N/A` |
| Stability | `N/A` neu chi 1 lan chay | `N/A` |
| Cost (USD) | `$X.XX` | `$0.37` |
| Expected Output | multi-line counts theo loai gap | `"Mismatch: 2\nMissing: 2\nSurplus: 2"` |
| Actual Output | multi-line counts theo loai gap | `"Mismatch: 6\nMissing: 1\nSurplus: 3"` |
| Logs | trich rieng object `total` tu log JSON, wrap trong dau ngoac kep | `"{ ... }"` |

**Format Expected Gap** (wrap trong dau ngoac kep trong CSV):
```
"N gaps
- MISMATCH-01: ...
- MISMATCH-02: ...
- MISSING-01: ...
- SURPLUS-01: ..."
```

**Format Actual Gap** (wrap trong dau ngoac kep trong CSV):
```
"(1) ...; (2) ...; (3) ..."
```

**Format Expected Output / Actual Output** (multi-line, wrap trong dau ngoac kep):
```
"Mismatch: X
Missing: X
Surplus: X"
```

Chi liet ke cac loai gap co xuat hien. Match co the dua trong narrative cua `Expected Gap`/`Actual Gap`, khong bat buoc liet ke trong `Expected Output` va `Actual Output`.

**Format Logs**
- Chi ghi phan `total` tu JSON log neu co.
- Giu xuong dong va escape dau ngoac kep theo CSV standard.
- Neu khong co log thi ghi `N/A`.

**Luu y CSV:** Moi field chua dau phay hoac xuat dong PHAI duoc boc trong dau ngoac kep `"..."`.
Neu khong co du lieu thi ghi `N/A`.
---

### Buoc 5: Bao cao

In ra tom tat ket qua:

```
## Ket qua danh gia {uc_id}

| Metric | Gia tri |
|--------|---------|
| TP | X |
| FP | X |
| FN | X |
| Precision | X.XXX (XX%) |
| Recall | X.XXX (XX%) |
| F1 Score | X.XX — [Danh gia] |
| Token | X (input) / X (output) |
| Latency | X.XX giay |
| Cost | $X.XX |

**Nhan xet:** [2-3 cau ve diem manh/yeu cua findgap trong test case nay]

→ Da dien vao `{csv_path}` dong STT={n}.
```

---

## Vi du tinh toan

**Expected:** "2 matching, 1 mismatch, 1 missing, 1 surplus"
**Actual (tu bang):** 4 match, 0 mismatch, 4 missing, 1 surplus

```
expected_gaps = 1 + 1 + 1 = 3
actual_gaps   = 0 + 4 + 1 = 5

TP = min(1,0) + min(1,4) + min(1,1) = 0 + 1 + 1 = 2
FP = 5 - 2 = 3
FN = 3 - 2 = 1

Precision = 2/5 = 0.400 (40%)
Recall    = 2/3 = 0.667 (67%)
F1        = 0.53 — Chap nhan duoc
```

**CSV row tuong ung:**
```
STT:            2
Use Case:       UC-02: View Challenge Detail
Gap Categories: Mixed
Token:          39,630
Recall:         0.833 (83%)
Precision:      0.500 (50%)
F1 Score:       0.63
Latency (s):    132.83
Security:       N/A
Stability:      N/A
Cost (USD):     $0.37
Expected Output:"Mismatch: 2
Missing: 2
Surplus: 2"
Actual Output:  "Mismatch: 6
Missing: 1
Surplus: 3"
Logs:           "      ""total"": {
                  ""input_tokens"": 28988,
                  ""output_tokens"": 10642,
                  ""cache_creation_tokens"": 31717,
                  ""cache_read_tokens"": 0,
                  ""duration_s"": 132.83,
                  ""cost_usd"": 0.3655
                }
              },"
```
