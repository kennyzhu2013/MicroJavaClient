/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     ProtobufRpcRequest   
 *  * @package    com.kennyzhu.micro.framework.protobuf  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/8 10:54  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
package com.kennyzhu.micro.framework.protobuf;
import com.google.common.primitives.Ints;
import com.google.protobuf.Message;
import com.kennyzhu.micro.framework.protobuf.RpcEnvelope;

public class ProtobufRpcRequest {
    private String serviceMethod;
    private Long sequenceNumber;
    private Message payload;

    public ProtobufRpcRequest(String serviceMethod, Message payload) {
        this.serviceMethod = serviceMethod;
        this.payload = payload;
    }

    public Message getPayload() {
        return payload;
    }

    public byte[] getProtobufData() {
        byte[] envelopeData = getEnvelope().toByteArray();
        byte[] payloadData = getPayload().toByteArray();
        int size = envelopeData.length + payloadData.length + 8;
        byte[] retval = new byte[size];
        int offset = 0;
        System.arraycopy(Ints.toByteArray(envelopeData.length), 0, retval, offset, 4);
        offset += 4;
        System.arraycopy(envelopeData, 0, retval, offset, envelopeData.length);
        offset += envelopeData.length;
        System.arraycopy(Ints.toByteArray(payloadData.length), 0, retval, offset, 4);
        offset += 4;
        System.arraycopy(payloadData, 0, retval, offset, payloadData.length);

        return retval;
    }

    private RpcEnvelope.Request getEnvelope() {
        RpcEnvelope.Request.Builder builder
                = RpcEnvelope.Request.newBuilder().setServiceMethod(serviceMethod);

        if (sequenceNumber != null) {
            builder.setSequenceNumber(sequenceNumber);
        }

        return builder.build();
    }
}
