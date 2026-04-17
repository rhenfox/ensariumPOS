package com.aldrin.ensarium.dispense;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HeldSaleStore {
    private final Path file;

    public HeldSaleStore() {
        this.file = Path.of(System.getProperty("user.home"), ".ensarium", "holds.ser");
    }

    public synchronized long save(HeldSale sale) throws IOException {
        Map<Long, HeldSale> all = readMap();
        long id = all.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1;
        HeldSale copy = copyOf(sale);
        copy.id = id;
        all.put(id, copy);
        writeMap(all);
        return id;
    }

    public synchronized List<HeldSale> list() throws IOException {
        List<HeldSale> out = new ArrayList<>();
        for (HeldSale held : readMap().values()) {
            out.add(copyOf(held));
        }
        out.sort(Comparator.comparing((HeldSale h) -> h.heldAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return out;
    }

    public synchronized HeldSale take(long id) throws IOException {
        Map<Long, HeldSale> all = readMap();
        HeldSale s = all.remove(id);
        writeMap(all);
        return s == null ? null : copyOf(s);
    }

    @SuppressWarnings("unchecked")
    private Map<Long, HeldSale> readMap() throws IOException {
        if (!Files.exists(file)) return new LinkedHashMap<>();
        try (InputStream in = Files.newInputStream(file); ObjectInputStream ois = new ObjectInputStream(in)) {
            Object obj = ois.readObject();
            return (Map<Long, HeldSale>) obj;
        } catch (EOFException e) {
            return new LinkedHashMap<>();
        } catch (InvalidClassException | StreamCorruptedException | OptionalDataException e) {
            backupCorruptFile();
            return new LinkedHashMap<>();
        } catch (ClassNotFoundException e) {
            backupCorruptFile();
            return new LinkedHashMap<>();
        }
    }


    private HeldSale copyOf(HeldSale src) {
        HeldSale dst = new HeldSale();
        dst.id = src.id;
        dst.heldAt = src.heldAt;
        dst.customerId = src.customerId;
        dst.customerName = src.customerName;
        if (src.saleDiscount != null) {
            dst.saleDiscount = src.saleDiscount.copy();
        }
        for (var line : src.lines) {
            dst.lines.add(line.copy());
        }
        return dst;
    }

    private void backupCorruptFile() throws IOException {
        if (!Files.exists(file)) return;
        String name = file.getFileName().toString();
        Path backup = file.resolveSibling(name + ".broken." + System.currentTimeMillis());
        Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
    }

    private void writeMap(Map<Long, HeldSale> map) throws IOException {
        Files.createDirectories(file.getParent());
        try (OutputStream out = Files.newOutputStream(file); ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(map);
        }
    }
}
