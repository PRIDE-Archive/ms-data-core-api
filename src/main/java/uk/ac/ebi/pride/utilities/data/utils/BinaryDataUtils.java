package uk.ac.ebi.pride.utilities.data.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * This class need to be deleted in the future.
 * <p/>
 * Endianess is often simply referred to as byte order
 * big endian is the most significant byte first. for example: 149 -> 10010101
 * little endian is the least significant byte first.
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 * Date: 29-Mar-2010
 * Time: 11:58:24
 */
public final class BinaryDataUtils {

    private static final Logger logger = LoggerFactory.getLogger(BinaryDataUtils.class);

    /**
     * Private Constructor
     */
    private BinaryDataUtils() {

    }

    /**
     * Convert a byte array to a number array
     *
     * @param byteArray byte array
     * @param dataType  data type
     * @param order     endianess
     * @return Number[]    number array
     */
    public static Number[] toNumberArray(byte[] byteArray, CvTermReference dataType, ByteOrder order) {
        int numOfByte = getNumOfByte(dataType);
        int arrLength = byteArray.length;
        Number[] results = new Number[arrLength / numOfByte];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);

        buffer.order(order);

        try {
            for (int i = 0; i < arrLength; i += numOfByte) {
                Number num;

                switch (dataType) {
                    case INT_32_BIT:
                        num = buffer.getInt(i);

                        break;

                    case FLOAT_16_BIT:
                    case FLOAT_32_BIT:
                        num = buffer.getFloat(i);

                        break;

                    case INT_64_BIT:
                        num = buffer.getLong(i);

                        break;

                    case FLOAT_64_BIT:
                        num = buffer.getDouble(i);

                        break;

                    default:
                        num = null;
                }

                results[i / numOfByte] = num;
            }
        } catch (Exception ex) {
            logger.error("Failed to byte array to number array: " + dataType.getName() + "\t" + order.toString());

            return new Number[0];
        }

        return results;
    }

    /**
     * Convert to double array
     *
     * @param arr      byte array
     * @param dataType data type
     * @param order    endianess
     * @return double[] double array
     */
    public static double[] toDoubleArray(byte[] arr, CvTermReference dataType, ByteOrder order) {
        Number[] numArr = toNumberArray(arr, dataType, order);
        double[] doubleArr = new double[numArr.length];

        for (int i = 0; i < numArr.length; i++) {
            doubleArr[i] = numArr[i].doubleValue();
        }

        return doubleArr;
    }

    /**
     * Get the number of bytes on a given data type
     *
     * @param dataType data type
     * @return int number of bytes
     */
    private static int getNumOfByte(CvTermReference dataType) {
        int numOfByte;

        switch (dataType) {
            case INT_32_BIT:
                numOfByte = 4;

                break;

            case FLOAT_16_BIT:
                numOfByte = 2;

                break;

            case FLOAT_32_BIT:
                numOfByte = 4;

                break;

            case INT_64_BIT:
                numOfByte = 8;

                break;

            case FLOAT_64_BIT:
                numOfByte = 8;

                break;

            default:
                numOfByte = -1;
        }

        return numOfByte;
    }

    /**
     * Decompress a compressed byte array
     *
     * @param compressedData compressed byte array
     * @return byte[]  decompressed data
     */
    public static byte[] decompress(byte[] compressedData) {
        byte[] decompressedData;

        // using a ByteArrayOutputStream to not having to define the result array size beforehand
        Inflater decompressor = new Inflater();

        decompressor.setInput(compressedData);

        // Create an expandable byte array to hold the decompressed data
        ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedData.length);
        byte[] buf = new byte[1024];

        while (!decompressor.finished()) {
            try {
                int count = decompressor.inflate(buf);
                if (count == 0 && decompressor.needsInput()) {
                    break;
                }
                bos.write(buf, 0, count);
            } catch (DataFormatException e) {
                throw new IllegalStateException("Encountered wrong data format "
                        + "while trying to decompress binary data!", e);
            }
        }

        try {
            bos.close();
        } catch (IOException e) {
            logger.error("Error while closing byte array output stream");
        }

        // Get the decompressed data
        decompressedData = bos.toByteArray();

        if (decompressedData == null) {
            throw new IllegalStateException("Decompression of binary data prodeuced no result (null)!");
        }

        return decompressedData;
    }

    /**
     * Compress byte array
     *
     * @param uncompressedData uncompressed byte array
     * @return byte[]  compressed byte array
     */
    public static byte[] compress(byte[] uncompressedData) {
        byte[] data;    // Decompress the data

        // create a temporary byte array big enough to hold the compressed data
        // with the worst compression (the length of the initial (uncompressed) data)
        byte[] temp = new byte[uncompressedData.length];

        // compress
        Deflater compresser = new Deflater();

        compresser.setInput(uncompressedData);
        compresser.finish();

        int cdl = compresser.deflate(temp);

        // create a new array with the size of the compressed data (cdl)
        data = new byte[cdl];
        System.arraycopy(temp, 0, data, 0, cdl);

        return data;
    }
}



