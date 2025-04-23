package me.mortaldev.jbeaster.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import me.mortaldev.jbeaster.Main;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ChanceMap<T> {
  LinkedHashMap<T, BigDecimal> table;

  @JsonCreator
  public ChanceMap(LinkedHashMap<T, BigDecimal> initialTable) {
    this.table = new LinkedHashMap<>(initialTable);
  }
  public ChanceMap() {
    table = new LinkedHashMap<>();
  }
  /**
   * Tells Jackson to serialize this ChanceMap object
   * *as if* it were just the internal map.
   *
   * @return The internal map for Jackson serialization.
   */
  @JsonValue // Tells Jackson to use this method's return value for serialization
  public LinkedHashMap<T, BigDecimal> getTableForSerialization() {
    // Ensure the internal table is never null when serializing
    return (this.table == null) ? new LinkedHashMap<>() : this.table;
  }

  @JsonIgnore // Ignore this for Jackson serialization/deserialization
  public synchronized Set<Map.Entry<T, BigDecimal>> getEntriesForIteration() {
    if (this.table == null) {
      // Return an immutable empty set if the table isn't initialized
      return Collections.emptySet();
    }
    // Return an unmodifiable view of the actual internal table's entry set
    return Collections.unmodifiableSet(this.table.entrySet());
  }

  @JsonIgnore
  public synchronized LinkedHashMap<T, BigDecimal> getTable() {
    return (this.table == null) ? new LinkedHashMap<>() : new LinkedHashMap<>(this.table);
  }

  public int size() {
    return table.size();
  }

  public T roll() {
    if (table.isEmpty()) {
      return null;
    }
    double generatedNumber = ThreadLocalRandom.current().nextDouble(0.00, 100.00);
    sort();
    LinkedHashMap<T, BigDecimal> reversedMap = Utils.reverseMap(table);
    BigDecimal total = BigDecimal.ZERO;
    for (Map.Entry<T, BigDecimal> entry : reversedMap.entrySet()) {
      total = total.add(entry.getValue());
      if (BigDecimal.valueOf(generatedNumber).compareTo(total) < 0) {
        return entry.getKey();
      }
    }
    return null;
  }

  public boolean updateKey(T key, BigDecimal newValue) {
    if (!table.containsKey(key)) {
      return false;
    }
    table.put(key, newValue);
    return true;
  }

  public boolean updateKey(T key, Number newValue) {
    return updateKey(key, new BigDecimal(newValue.toString()));
  }

  public boolean updateKey(T key, String newValue) {
    return updateKey(key, new BigDecimal(newValue));
  }

  public boolean balanceTable() {
    if (isBalanced()) {
      return false;
    }

    if (table.isEmpty()) {
      return false;
    }

    BigDecimal sum = BigDecimal.ZERO;
    for (BigDecimal value : table.values()) {
      sum = sum.add(value);
    }
    // Not Zero
    if (sum.compareTo(BigDecimal.ZERO) == 0) {
      Main.log("ERROR! '0' found in ChanceMap");
      return false;
    }
    LinkedHashMap<T, BigDecimal> newTable = new LinkedHashMap<>(table);
    int i = 0;
    int lastIteration = table.values().size() - 1;
    BigDecimal total = BigDecimal.ZERO;
    for (Map.Entry<T, BigDecimal> entry : newTable.entrySet()) {
      BigDecimal scaledValue;
      if (i == lastIteration) {
        scaledValue = new BigDecimal(1).subtract(total);
      } else {
        scaledValue = entry.getValue().divide(sum, 2, RoundingMode.HALF_UP);
      }
      total = total.add(scaledValue);
      entry.setValue(scaledValue.multiply(new BigDecimal(100)));
      i++;
    }
    table = newTable;
    return true;
  }

  public synchronized void sort() {
    if (table.isEmpty()) {
      return;
    }

    LinkedHashMap<T, BigDecimal> sortedMap =
        table.entrySet().stream()
            .sorted(Map.Entry.<T, BigDecimal>comparingByValue().reversed())
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue,
                    LinkedHashMap::new));
    table = new LinkedHashMap<>(sortedMap);
  }

  public boolean isBalanced() {
    return getTotal().compareTo(new BigDecimal(100)) == 0;
  }

  public synchronized BigDecimal getTotal() {
    if (table.isEmpty()) {
      return new BigDecimal("-1");
    }

    BigDecimal total = BigDecimal.ZERO;
    for (BigDecimal value : table.values()) {
      if (value == null) {
        continue;
      }
      total = total.add(value);
    }
    return total.setScale(2, RoundingMode.HALF_UP);
  }

  public synchronized void put(T key, boolean balanceAfter) {
    BigDecimal percent;
    if (table.isEmpty()) {
      percent = new BigDecimal("100");
    } else {
      BigDecimal size = new BigDecimal(table.size());
      BigDecimal hundred = new BigDecimal(100);
      percent = hundred.divide(size, 2, RoundingMode.HALF_UP);
    }
    put(key, percent, balanceAfter);
  }

  public synchronized void put(T key, Number amount, boolean balanceAfter) {
    put(key, new BigDecimal(amount.toString()), balanceAfter);
  }

  public synchronized void put(T key, BigDecimal amount, boolean balanceAfter) {
    table.put(key, amount);
    if (balanceAfter) {
      balanceTable();
    }
  }

  public synchronized void remove(T key, boolean balanceAfter) {
    if (table.isEmpty()) {
      return;
    }
    table.remove(key);
    if (balanceAfter) {
      balanceTable();
    }
  }

  @JsonIgnore
  public synchronized void setTable(LinkedHashMap<T, BigDecimal> table) {
    this.table = table;
  }
}
