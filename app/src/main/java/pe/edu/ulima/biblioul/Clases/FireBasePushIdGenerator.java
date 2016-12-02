package pe.edu.ulima.biblioul.Clases;

public class FireBasePushIdGenerator {
    static final String PUSH_CHARS = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz";
    static long LAST_PUSH_TIME = 0;
    static int[] LAST_RANDOM_CHAR_IDXS= new int[12];
    static char[] ID = new char[20];

    public static String getFirebaseId() {
        long now = System.currentTimeMillis();
        boolean duplicateTime = now==LAST_PUSH_TIME;
        LAST_PUSH_TIME = now;

        if(!duplicateTime) {
            for(int i = 7; i >= 0; i--) {
                ID[i]=PUSH_CHARS.charAt((int) (now%64));
                now = (long) Math.floor(now / 64);
            }
        }

        if(!duplicateTime) {
            for (int i = 0; i < 12; i++) {
                LAST_RANDOM_CHAR_IDXS[i]=(int) Math.floor(Math.random() * 64);
                ID[8+i]=PUSH_CHARS.charAt(LAST_RANDOM_CHAR_IDXS[i]);
            }
        } else {
            int i = 11;
            for (; i >= 0 && LAST_RANDOM_CHAR_IDXS[i] == 63; i--) {
                LAST_RANDOM_CHAR_IDXS[i]=0;
            }
            LAST_RANDOM_CHAR_IDXS[i]++;
        }
        for(int i = 0; i < 12; i++) {
            ID[8+i]=PUSH_CHARS.charAt(LAST_RANDOM_CHAR_IDXS[i]);
        }
        return String.valueOf(ID);
    }
}