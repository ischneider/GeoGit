/* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the LGPL 2.1 license, available at the root
 * application directory.
 */
package org.geogit.storage.hessian;

import java.io.IOException;
import java.util.List;

import org.geogit.api.ObjectId;
import org.geogit.api.RevFeatureType;
import org.geogit.api.RevObject;
import org.geogit.storage.GtEntityType;
import org.geogit.storage.ObjectReader;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

import com.caucho.hessian.io.Hessian2Input;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * Reads {@link RevFeatureType feature types} from a binary encoded stream.
 */
public class HessianSimpleFeatureTypeReader extends HessianRevReader<RevFeatureType> implements
        ObjectReader<RevFeatureType> {

    private SimpleFeatureTypeBuilder builder;

    private FeatureTypeFactory typeFactory;

    /**
     * Constructs a new {@code HessianSimpleFeatureTypeReader}.
     */
    public HessianSimpleFeatureTypeReader() {
        this.builder = new SimpleFeatureTypeBuilder();
        this.typeFactory = builder.getFeatureTypeFactory();
    }

    /**
     * Reads a {@link RevFeatureType} from the given input stream and assigns it the provided
     * {@link ObjectId id}.
     * 
     * @param id the id to use for the feature type
     * @param rawData the input stream of the feature type
     * @return the final feature type
     * @throws IllegalArgumentException if the provided stream does not represent a
     *         {@code RevFeatureType}
     */
    @Override
    protected RevFeatureType read(ObjectId id, Hessian2Input hin, RevObject.TYPE blobType)
            throws IOException {
        Preconditions.checkArgument(RevObject.TYPE.FEATURETYPE.equals(blobType));

        String typeNamespace = hin.readString();
        String typeName = hin.readString();
        int attributeCount = hin.readInt();
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        for (int i = 0; i < attributeCount; i++) {
            try {
                builder.add(readDescriptor(hin));
            } catch (FactoryException ex) {
                Throwables.propagate(ex);
            }
        }
        builder.setName(new NameImpl("".equals(typeNamespace) ? null : typeNamespace, typeName));
        SimpleFeatureType type = builder.buildFeatureType();
        return new RevFeatureType(id, type);
    }

    /**
     * The format will be written as follows:
     * <ol>
     * <li>EntityType - int</li>
     * <li>nillable - boolean</li>
     * <li>property namespace - String</li>
     * <li>property name - String</li>
     * <li>max - int</li>
     * <li>min - int</li>
     * <li>type namespace - String</li>
     * <li>type name - String</li>
     * </ol>
     * If the entity type is a geometry, then there is an additional (srid - String) field at the
     * end.
     * 
     * @param hout
     */
    @SuppressWarnings("rawtypes")
    private AttributeDescriptor readDescriptor(Hessian2Input hin) throws IOException,
            FactoryException {
        int typeValue = hin.readInt();
        GtEntityType type = GtEntityType.fromValue(typeValue);
        Class binding = type.getBinding();
        boolean nillable = hin.readBoolean();
        String pNamespace = hin.readString();
        String pName = hin.readString();
        int maxOccurs = hin.readInt();
        int minOccurs = hin.readInt();
        String tNamespace = hin.readString();
        String tName = hin.readString();
        String crsText = null;
        boolean crsCode = false;
        if (type.isGeometry()) {
            Object bObj = hin.readObject();
            if (bObj instanceof Class) {
                binding = (Class) bObj;
            }
            crsCode = hin.readBoolean();
            crsText = hin.readString();
        }

        /*
         * Default values that are currently not encoded.
         */
        boolean isIdentifiable = false;
        boolean isAbstract = false;
        List<Filter> restrictions = null;
        AttributeType superType = null;
        InternationalString description = null;
        Object defaultValue = null;

        Name propertyName = new NameImpl("".equals(pNamespace) ? null : pNamespace, pName);
        Name typeName = new NameImpl("".equals(tNamespace) ? null : tNamespace, tName);

        AttributeType attributeType;
        AttributeDescriptor attributeDescriptor;
        if (type.isGeometry()) {
            CoordinateReferenceSystem crs;
            if (crsCode) {
                if ("urn:ogc:def:crs:EPSG::0".equals(crsText)) {
                    crs = null;
                } else {
                    boolean forceLongitudeFirst = crsText.startsWith("EPSG:");
                    crs = CRS.decode(crsText, forceLongitudeFirst);
                }
            } else {
                crs = CRS.parseWKT(crsText);
            }
            attributeType = typeFactory.createGeometryType(typeName, binding, crs, isIdentifiable,
                    isAbstract, restrictions, superType, description);
            attributeDescriptor = typeFactory.createGeometryDescriptor(
                    (GeometryType) attributeType, propertyName, minOccurs, maxOccurs, nillable,
                    defaultValue);
        } else {
            attributeType = typeFactory.createAttributeType(typeName, binding, isIdentifiable,
                    isAbstract, restrictions, superType, description);
            attributeDescriptor = typeFactory.createAttributeDescriptor(attributeType,
                    propertyName, minOccurs, maxOccurs, nillable, defaultValue);
        }
        return attributeDescriptor;

    }

}
