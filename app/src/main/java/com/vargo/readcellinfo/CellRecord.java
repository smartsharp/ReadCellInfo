package com.vargo.readcellinfo;

public class CellRecord {
    public int id;
    public int type;
    public String location;
    public String cells;
    public String neighbors;

    private static final String SEP_CHAR = "~";
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(SEP_CHAR);
        sb.append(type).append(SEP_CHAR);
        sb.append(location).append(SEP_CHAR);
        sb.append(cells).append(SEP_CHAR);
        sb.append(neighbors);
        return sb.toString();
    }
    public static CellRecord fromString(String encoded) {
        if(encoded == null || encoded.length()==0) return null;
        String[] arrs = encoded.split(SEP_CHAR);
        if(arrs == null || arrs.length != 5) return null;
        CellRecord r = new CellRecord();
        try {
            r.id = Integer.parseInt(arrs[0]);
        }catch (Exception e){}
        try {
            r.type = Integer.parseInt(arrs[1]);
        }catch (Exception e){}
        r.location = arrs[2];
        if(r.location == null || r.location.length() == 0 || r.location.equals("null")) return null;
        r.cells = arrs[3];
        if(r.cells == null || r.cells.length() == 0 || r.cells.equals("null")) r.cells = null;
        r.neighbors = arrs[4];
        if(r.neighbors == null || r.neighbors.length() == 0 || r.neighbors.equals("null")) r.neighbors = null;

        return r;
    }
}
