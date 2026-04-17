package com.aldrin.ensarium.stockin;

import java.util.ArrayList;
import java.util.List;

public class LoadedStockIn {

    private StockInHeader header;
    private List<StockInLine> lines = new ArrayList<>();

    public StockInHeader getHeader() {
        return header;
    }

    public void setHeader(StockInHeader header) {
        this.header = header;
    }

    public List<StockInLine> getLines() {
        return lines;
    }

    public void setLines(List<StockInLine> lines) {
        this.lines = lines;
    }
}
