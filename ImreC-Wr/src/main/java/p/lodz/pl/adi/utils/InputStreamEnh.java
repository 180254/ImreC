package p.lodz.pl.adi.utils;

import java.io.InputStream;

public class InputStreamEnh {

    private InputStream is;
    private long isLength;

    public InputStreamEnh(InputStream is, long isLength) {
        this.is = is;
        this.isLength = isLength;
    }

    public InputStream getIs() {
        return is;
    }

    public long getIsLength() {
        return isLength;
    }
}
