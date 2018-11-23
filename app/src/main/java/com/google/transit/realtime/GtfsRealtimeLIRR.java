/**
 * Portions of this module are modifications based on work created and shared by Google:
 *
 *   http://code.google.com/policies.html
 *
 * and used according to terms described in the Creative Commons 3.0 Attribution License:
 *
 *   http://creativecommons.org/licenses/by/3.0/
 *
 * Specifically, the gtfs-realtime.proto:
 *
 *   http://code.google.com/transit/realtime/docs/gtfs-realtime_proto.html
 *
 * was used to generate the classes contained within.
 *
 * Portions of this module are modifications based on work created by the New York
 * City Metropolitan Transportation Authority.
 *
 * All code is Copyright (C) 2011 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: com/google/transit/realtime/gtfs-realtime-LIRR.proto

package com.google.transit.realtime;

public final class GtfsRealtimeLIRR {
  private GtfsRealtimeLIRR() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registry.add(MtaStopTimeUpdate.track);
  }
  public interface MtaStopTimeUpdateOrBuilder extends
      // @@protoc_insertion_point(interface_extends:transit_realtime.MtaStopTimeUpdate)
      com.google.protobuf.MessageOrBuilder {
  }
  /**
   * Protobuf type {@code transit_realtime.MtaStopTimeUpdate}
   */
  public static final class MtaStopTimeUpdate extends
      com.google.protobuf.GeneratedMessage implements
      // @@protoc_insertion_point(message_implements:transit_realtime.MtaStopTimeUpdate)
      MtaStopTimeUpdateOrBuilder {
    // Use MtaStopTimeUpdate.newBuilder() to construct.
    private MtaStopTimeUpdate(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private MtaStopTimeUpdate(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final MtaStopTimeUpdate defaultInstance;
    public static MtaStopTimeUpdate getDefaultInstance() {
      return defaultInstance;
    }

    public MtaStopTimeUpdate getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private MtaStopTimeUpdate(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return GtfsRealtimeLIRR.internal_static_transit_realtime_MtaStopTimeUpdate_descriptor;
    }

    protected FieldAccessorTable
        internalGetFieldAccessorTable() {
      return GtfsRealtimeLIRR.internal_static_transit_realtime_MtaStopTimeUpdate_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              MtaStopTimeUpdate.class, Builder.class);
    }

    public static com.google.protobuf.Parser<MtaStopTimeUpdate> PARSER =
        new com.google.protobuf.AbstractParser<MtaStopTimeUpdate>() {
      public MtaStopTimeUpdate parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new MtaStopTimeUpdate(input, extensionRegistry);
      }
    };

    @Override
    public com.google.protobuf.Parser<MtaStopTimeUpdate> getParserForType() {
      return PARSER;
    }

    private void initFields() {
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @Override
    protected Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static MtaStopTimeUpdate parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static MtaStopTimeUpdate parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static MtaStopTimeUpdate parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static MtaStopTimeUpdate parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static MtaStopTimeUpdate parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static MtaStopTimeUpdate parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static MtaStopTimeUpdate parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static MtaStopTimeUpdate parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static MtaStopTimeUpdate parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static MtaStopTimeUpdate parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(MtaStopTimeUpdate prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @Override
    protected Builder newBuilderForType(
        BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code transit_realtime.MtaStopTimeUpdate}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:transit_realtime.MtaStopTimeUpdate)
        MtaStopTimeUpdateOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return GtfsRealtimeLIRR.internal_static_transit_realtime_MtaStopTimeUpdate_descriptor;
      }

      protected FieldAccessorTable
          internalGetFieldAccessorTable() {
        return GtfsRealtimeLIRR.internal_static_transit_realtime_MtaStopTimeUpdate_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                MtaStopTimeUpdate.class, Builder.class);
      }

      // Construct using com.google.transit.realtime.GtfsRealtimeLIRR.MtaStopTimeUpdate.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return GtfsRealtimeLIRR.internal_static_transit_realtime_MtaStopTimeUpdate_descriptor;
      }

      public MtaStopTimeUpdate getDefaultInstanceForType() {
        return MtaStopTimeUpdate.getDefaultInstance();
      }

      public MtaStopTimeUpdate build() {
        MtaStopTimeUpdate result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public MtaStopTimeUpdate buildPartial() {
        MtaStopTimeUpdate result = new MtaStopTimeUpdate(this);
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof MtaStopTimeUpdate) {
          return mergeFrom((MtaStopTimeUpdate)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(MtaStopTimeUpdate other) {
        if (other == MtaStopTimeUpdate.getDefaultInstance()) return this;
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        MtaStopTimeUpdate parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (MtaStopTimeUpdate) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      // @@protoc_insertion_point(builder_scope:transit_realtime.MtaStopTimeUpdate)
    }

    static {
      defaultInstance = new MtaStopTimeUpdate(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:transit_realtime.MtaStopTimeUpdate)
    public static final int TRACK_FIELD_NUMBER = 1005;
    /**
     * <code>extend .transit_realtime.TripUpdate.StopTimeUpdate { ... }</code>
     *
     * <pre>
     *can add additional fields here without having to extend StopTimeUpdate again
     * </pre>
     */
    public static final
      GeneratedExtension<
        GtfsRealtime.TripUpdate.StopTimeUpdate,
        String> track = com.google.protobuf.GeneratedMessage
            .newMessageScopedGeneratedExtension(
          MtaStopTimeUpdate.getDefaultInstance(),
          0,
          String.class,
          null);
  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_transit_realtime_MtaStopTimeUpdate_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_transit_realtime_MtaStopTimeUpdate_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    String[] descriptorData = {
      "\n4com/google/transit/realtime/gtfs-realt" +
      "ime-LIRR.proto\022\020transit_realtime\032/com/go" +
      "ogle/transit/realtime/gtfs-realtime.prot" +
      "o\"P\n\021MtaStopTimeUpdate2;\n\005track\022+.transi" +
      "t_realtime.TripUpdate.StopTimeUpdate\030\355\007 " +
      "\001(\tB\035\n\033com.google.transit.realtime"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          GtfsRealtime.getDescriptor(),
        }, assigner);
    internal_static_transit_realtime_MtaStopTimeUpdate_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_transit_realtime_MtaStopTimeUpdate_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_transit_realtime_MtaStopTimeUpdate_descriptor,
        new String[] { });
    GtfsRealtime.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
