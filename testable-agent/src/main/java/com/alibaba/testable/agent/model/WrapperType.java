package com.alibaba.testable.agent.model;

/**
 * simplified of sun.invoke.util.Wrapper
 */
public enum WrapperType {

    //        wrapperType    primitiveType  char       format
    BOOLEAN(  Boolean.class, boolean.class, 'Z', Format.unsigned( 1)),
    // These must be in the order defined for widening primitive conversions in JLS 5.1.2
    BYTE   (     Byte.class,    byte.class, 'B', Format.signed(   8)),
    SHORT  (    Short.class,   short.class, 'S', Format.signed(  16)),
    CHAR   (Character.class,    char.class, 'C', Format.unsigned(16)),
    INT    (  Integer.class,     int.class, 'I', Format.signed(  32)),
    LONG   (     Long.class,    long.class, 'J', Format.signed(  64)),
    FLOAT  (    Float.class,   float.class, 'F', Format.floating(32)),
    DOUBLE (   Double.class,  double.class, 'D', Format.floating(64)),
    OBJECT (   Object.class,  Object.class, 'L', Format.other(    1)),
    // VOID must be the last type, since it is "assignable" from any other type:
    VOID   (     Void.class,    void.class, 'V', Format.other(    0)),
    ;

    private final Class<?> wrapperType;
    private final Class<?> primitiveType;
    private final char     basicTypeChar;
    private final int      format;

    WrapperType(Class<?> wtype, Class<?> ptype, char tchar, int format) {
        this.wrapperType = wtype;
        this.primitiveType = ptype;
        this.basicTypeChar = tchar;
        this.format = format;
    }

    private static abstract class Format {
        static final int SLOT_SHIFT = 0, SIZE_SHIFT = 2, KIND_SHIFT = 12;
        static final int
                SIGNED   = (-1) << KIND_SHIFT,
                UNSIGNED = 0    << KIND_SHIFT,
                FLOATING = 1    << KIND_SHIFT;
        static final int
                SLOT_MASK = ((1<<(SIZE_SHIFT-SLOT_SHIFT))-1),
                SIZE_MASK = ((1<<(KIND_SHIFT-SIZE_SHIFT))-1);
        static int format(int kind, int size, int slots) {
            assert(((kind >> KIND_SHIFT) << KIND_SHIFT) == kind);
            assert((size & (size-1)) == 0); // power of two
            assert((kind == SIGNED)   ? (size > 0) : (kind == UNSIGNED) ? (size > 0) : (kind == FLOATING) ? (size == 32 || size == 64)  : false);
            assert((slots == 2) ? (size == 64) : (slots == 1) ? (size <= 32) : false);
            return kind | (size << SIZE_SHIFT) | (slots << SLOT_SHIFT);
        }
        static final int
                INT      = SIGNED   | (32 << SIZE_SHIFT) | (1 << SLOT_SHIFT),
                SHORT    = SIGNED   | (16 << SIZE_SHIFT) | (1 << SLOT_SHIFT),
                BOOLEAN  = UNSIGNED | (1  << SIZE_SHIFT) | (1 << SLOT_SHIFT),
                CHAR     = UNSIGNED | (16 << SIZE_SHIFT) | (1 << SLOT_SHIFT),
                FLOAT    = FLOATING | (32 << SIZE_SHIFT) | (1 << SLOT_SHIFT),
                VOID     = UNSIGNED | (0  << SIZE_SHIFT) | (0 << SLOT_SHIFT),
                NUM_MASK = (-1) << SIZE_SHIFT;
        static int signed(int size)   { return format(SIGNED,   size, (size > 32 ? 2 : 1)); }
        static int unsigned(int size) { return format(UNSIGNED, size, (size > 32 ? 2 : 1)); }
        static int floating(int size) { return format(FLOATING, size, (size > 32 ? 2 : 1)); }
        static int other(int slots)   { return slots << SLOT_SHIFT; }
    }

    /// format queries:

    /** How many bits are in the wrapped value?  Returns 0 for OBJECT or VOID. */
    public int     bitWidth()      { return (format >> Format.SIZE_SHIFT) & Format.SIZE_MASK; }
    /** How many JVM stack slots occupied by the wrapped value?  Returns 0 for VOID. */
    public int     stackSlots()    { return (format >> Format.SLOT_SHIFT) & Format.SLOT_MASK; }
    /** Does the wrapped value occupy a single JVM stack slot? */
    public boolean isSingleWord()  { return (format & (1 << Format.SLOT_SHIFT)) != 0; }
    /** Does the wrapped value occupy two JVM stack slots? */
    public boolean isDoubleWord()  { return (format & (2 << Format.SLOT_SHIFT)) != 0; }
    /** Is the wrapped type numeric (not void or object)? */
    public boolean isNumeric()     { return (format & Format.NUM_MASK) != 0; }
    /** Is the wrapped type a primitive other than float, double, or void? */
    public boolean isIntegral()    { return isNumeric() && format < Format.FLOAT; }
    /** Is the wrapped type one of int, boolean, byte, char, or short? */
    public boolean isSubwordOrInt() { return isIntegral() && isSingleWord(); }
    /* Is the wrapped value a signed integral type (one of byte, short, int, or long)? */
    public boolean isSigned()      { return format < Format.VOID; }
    /* Is the wrapped value an unsigned integral type (one of boolean or char)? */
    public boolean isUnsigned()    { return format >= Format.BOOLEAN && format < Format.FLOAT; }
    /** Is the wrapped type either float or double? */
    public boolean isFloating()    { return format >= Format.FLOAT; }
    /** Is the wrapped type either void or a reference? */
    public boolean isOther()       { return (format & ~Format.SLOT_MASK) == 0; }

    /** Does the JLS 5.1.2 allow a variable of this wrapper's
     *  primitive type to be assigned from a value of the given wrapper's primitive type?
     *  Cases:
     *  <ul>
     *  <li>unboxing followed by widening primitive conversion
     *  <li>any type converted to {@code void} (i.e., dropping a method call's value)
     *  <li>boxing conversion followed by widening reference conversion to {@code Object}
     *  </ul>
     *  These are the cases allowed by MethodHandle.asType.
     */
    public boolean isConvertibleFrom(WrapperType source) {
        if (this == source)  return true;
        if (this.compareTo(source) < 0) {
            // At best, this is a narrowing conversion.
            return false;
        }
        // All conversions are allowed in the enum order between floats and signed ints.
        // First detect non-signed non-float types (boolean, char, Object, void).
        boolean floatOrSigned = (((this.format & source.format) & Format.SIGNED) != 0);
        if (!floatOrSigned) {
            if (this.isOther())  return true;
            // can convert char to int or wider, but nothing else
            return source.format == Format.CHAR;
            // no other conversions are classified as widening
        }
        // All signed and float conversions in the enum order are widening.
        assert(this.isFloating() || this.isSigned());
        assert(source.isFloating() || source.isSigned());
        return true;
    }

    static { assert(checkConvertibleFrom()); }
    private static boolean checkConvertibleFrom() {
        // Check the matrix for correct classification of widening conversions.
        for (WrapperType w : values()) {
            assert(w.isConvertibleFrom(w));
            assert(VOID.isConvertibleFrom(w));
            if (w != VOID) {
                assert(OBJECT.isConvertibleFrom(w));
                assert(!w.isConvertibleFrom(VOID));
            }
            // check relations with unsigned integral types:
            if (w != CHAR) {
                assert(!CHAR.isConvertibleFrom(w));
                assert w.isConvertibleFrom(INT) || (!w.isConvertibleFrom(CHAR));
            }
            if (w != BOOLEAN) {
                assert(!BOOLEAN.isConvertibleFrom(w));
                assert w == VOID || w == OBJECT || (!w.isConvertibleFrom(BOOLEAN));
            }
            // check relations with signed integral types:
            if (w.isSigned()) {
                for (WrapperType x : values()) {
                    if (w == x)  continue;
                    if (x.isFloating())
                        assert(!w.isConvertibleFrom(x));
                    else if (x.isSigned()) {
                        if (w.compareTo(x) < 0)
                            assert(!w.isConvertibleFrom(x));
                        else
                            assert(w.isConvertibleFrom(x));
                    }
                }
            }
            // check relations with floating types:
            if (w.isFloating()) {
                for (WrapperType x : values()) {
                    if (w == x)  continue;
                    if (x.isSigned())
                        assert(w.isConvertibleFrom(x));
                    else if (x.isFloating()) {
                        if (w.compareTo(x) < 0)
                            assert(!w.isConvertibleFrom(x));
                        else
                            assert(w.isConvertibleFrom(x));
                    }
                }
            }
        }
        return true;  // i.e., assert(true)
    }

    // Note on perfect hashes:
    //   for signature chars c, do (c + (c >> 1)) % 16
    //   for primitive type names n, do (n[0] + n[2]) % 16
    // The type name hash works for both primitive and wrapper names.
    // You can add "java/lang/Object" to the primitive names.
    // But you add the wrapper name Object, use (n[2] + (3*n[1])) % 16.
    private static final WrapperType[] FROM_PRIM = new WrapperType[16];
    private static final WrapperType[] FROM_WRAP = new WrapperType[16];
    private static final WrapperType[] FROM_CHAR = new WrapperType[16];
    private static int hashPrim(Class<?> x) {
        String xn = x.getName();
        if (xn.length() < 3)  return 0;
        return (xn.charAt(0) + xn.charAt(2)) % 16;
    }
    private static int hashWrap(Class<?> x) {
        String xn = x.getName();
        final int offset = 10;
        if (xn.length() < offset+3)  return 0;
        return (3*xn.charAt(offset+1) + xn.charAt(offset+2)) % 16;
    }
    private static int hashChar(char x) {
        return (x + (x >> 1)) % 16;
    }
    static {
        for (WrapperType w : values()) {
            int pi = hashPrim(w.primitiveType);
            int wi = hashWrap(w.wrapperType);
            int ci = hashChar(w.basicTypeChar);
            assert(FROM_PRIM[pi] == null);
            assert(FROM_WRAP[wi] == null);
            assert(FROM_CHAR[ci] == null);
            FROM_PRIM[pi] = w;
            FROM_WRAP[wi] = w;
            FROM_CHAR[ci] = w;
        }
    }

    /** Wrap a value in this wrapper's type.
     * Performs standard primitive conversions, including truncation and float conversions.
     * Performs returns the unchanged reference for {@code OBJECT}.
     * Returns null for {@code VOID}.
     * Returns a zero value for a null input.
     * @throws ClassCastException if this wrapper is numeric and the operand
     *                            is not a number, character, boolean, or null
     */
    public Object wrap(Object x) {
        // do non-numeric wrappers first
        switch (basicTypeChar) {
            case 'L': return x;
            case 'V': return null;
        }
        Number xn = numberValue(x);
        switch (basicTypeChar) {
            case 'I': return xn.intValue();
            case 'J': return xn.longValue();
            case 'F': return xn.floatValue();
            case 'D': return xn.doubleValue();
            case 'S': return (short) xn.intValue();
            case 'B': return (byte) xn.intValue();
            case 'C': return (char) xn.intValue();
            case 'Z': return boolValue(xn.byteValue());
        }
        throw new InternalError("bad wrapper");
    }

    /** Wrap a value (an int or smaller value) in this wrapper's type.
     * Performs standard primitive conversions, including truncation and float conversions.
     * Produces an {@code Integer} for {@code OBJECT}, although the exact type
     * of the operand is not known.
     * Returns null for {@code VOID}.
     */
    public Object wrap(int x) {
        if (basicTypeChar == 'L')  return x;
        switch (basicTypeChar) {
            case 'L': throw new IllegalArgumentException("cannot wrap to object type");
            case 'V': return null;
            case 'I': return x;
            case 'J': return (long) x;
            case 'F': return (float) x;
            case 'D': return (double) x;
            case 'S': return (short) x;
            case 'B': return (byte) x;
            case 'C': return (char) x;
            case 'Z': return boolValue((byte) x);
        }
        throw new InternalError("bad wrapper");
    }

    private static Number numberValue(Object x) {
        if (x instanceof Number)     return (Number)x;
        if (x instanceof Character)  return (int)(Character)x;
        if (x instanceof Boolean)    return (Boolean)x ? 1 : 0;
        // Remaining allowed case of void:  Must be a null reference.
        return (Number)x;
    }

    // Parameter type of boolValue must be byte, because
    // MethodHandles.explicitCastArguments defines boolean
    // conversion as first converting to byte.
    private static boolean boolValue(byte bits) {
        bits &= 1;  // simple 31-bit zero extension
        return (bits != 0);
    }

}
