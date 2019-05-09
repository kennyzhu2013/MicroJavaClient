/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     ProtobufRpcResponse   
 *  * @package    com.kennyzhu.micro.framework.protobuf  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/8 10:58  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
package com.kennyzhu.micro.framework.protobuf;

import com.google.common.primitives.Ints;
import com.kennyzhu.micro.framework.rpc.exception.RpcCallException;
import com.kennyzhu.micro.framework.protobuf.RpcEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ProtobufRpcResponse {
    public final static int MAX_HEADER_SIZE = 1048576;
    private static final Logger logger = LoggerFactory.getLogger(ProtobufRpcResponse.class);
    private String serviceMethod;
    private long sequenceNumber;
    private String errorMessage;
    private byte payloadData[];

    public ProtobufRpcResponse(byte[] data) throws RpcCallException {
        // we may get a json error even though we make a protobuf call
        if (data[0] == '{' && data[data.length - 1] == '}') {
            readRpcError(new String(data));
            return;
        }
        int headerLength = Ints.fromByteArray(data);
        if (headerLength < 0 || headerLength > MAX_HEADER_SIZE) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unexpected header length: ").append(headerLength).
                    append(", data: ").append(ensurePrintable(data, 256));
            String message = sb.toString();
            logger.warn(message);
            throw new RpcCallException(RpcCallException.Category.InternalServerError, message);
        }
        logger.debug("headerLength = {}", headerLength);
        int offset = 4;
        byte headerData[] = Arrays.copyOfRange(data, offset, offset + headerLength);
        offset += headerLength;
        byte payloadLengthBuffer[] = Arrays.copyOfRange(data, offset, offset + 4);
        offset += 4;
        int payloadLength = Ints.fromByteArray(payloadLengthBuffer);
        payloadData = Arrays.copyOfRange(data, offset, offset + payloadLength);
        RpcEnvelope.Response responseHeader = ProtobufUtil.
                byteArrayToProtobuf(headerData, RpcEnvelope.Response.class);
        serviceMethod = responseHeader.getServiceMethod();
        sequenceNumber = responseHeader.getSequenceNumber();
        errorMessage = responseHeader.getError();
    }

    /**
     * Intended only for testing
     */
    public ProtobufRpcResponse(String serviceMethod, long sequenceNumber,
                               String errorMessage, byte[] payloadData) {
        this.serviceMethod = serviceMethod;
        this.sequenceNumber = sequenceNumber;
        this.errorMessage = errorMessage;
        this.payloadData = payloadData;
    }

    //array printer which shows non-ascii characters as their hex value
    protected static String ensurePrintable(byte[] data, int maxLength) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int length = data.length;
        for (int i = 0; i < length && i < maxLength; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            byte b = data[i];
            if (b > 32) {
                sb.append((char) data[i]);
            } else {
                sb.append("0x").append(Integer.toHexString(b & 0x000000ff));
            }
        }
        if (length > maxLength) {
            sb.append(", ...");
        }
        sb.append(']');
        return sb.toString();
    }

    private void readRpcError(String data) {
        try {
            // JsonRpcResponse json = JsonRpcResponse.fromString(data);
            this.errorMessage = data;
        } catch (Exception ex) {
            logger.warn("Caught exception parsing response");
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public byte[] getPayloadData() {
        return payloadData;
    }
}
