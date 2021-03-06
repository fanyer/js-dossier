// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: dossier.proto

package com.github.jsdossier.proto;

public interface JsTypeOrBuilder extends
    // @@protoc_insertion_point(interface_extends:dossier.JsType)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional string name = 1;</code>
   */
  java.lang.String getName();
  /**
   * <code>optional string name = 1;</code>
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>optional .dossier.SourceLink source = 5;</code>
   */
  boolean hasSource();
  /**
   * <code>optional .dossier.SourceLink source = 5;</code>
   */
  com.github.jsdossier.proto.SourceLink getSource();
  /**
   * <code>optional .dossier.SourceLink source = 5;</code>
   */
  com.github.jsdossier.proto.SourceLinkOrBuilder getSourceOrBuilder();

  /**
   * <code>optional .dossier.JsType.NestedTypes nested = 6;</code>
   */
  boolean hasNested();
  /**
   * <code>optional .dossier.JsType.NestedTypes nested = 6;</code>
   */
  com.github.jsdossier.proto.JsType.NestedTypes getNested();
  /**
   * <code>optional .dossier.JsType.NestedTypes nested = 6;</code>
   */
  com.github.jsdossier.proto.JsType.NestedTypesOrBuilder getNestedOrBuilder();

  /**
   * <code>optional .dossier.Comment description = 7;</code>
   */
  boolean hasDescription();
  /**
   * <code>optional .dossier.Comment description = 7;</code>
   */
  com.github.jsdossier.proto.Comment getDescription();
  /**
   * <code>optional .dossier.Comment description = 7;</code>
   */
  com.github.jsdossier.proto.CommentOrBuilder getDescriptionOrBuilder();

  /**
   * <code>optional .dossier.Tags tags = 8;</code>
   */
  boolean hasTags();
  /**
   * <code>optional .dossier.Tags tags = 8;</code>
   */
  com.github.jsdossier.proto.Tags getTags();
  /**
   * <code>optional .dossier.Tags tags = 8;</code>
   */
  com.github.jsdossier.proto.TagsOrBuilder getTagsOrBuilder();

  /**
   * <code>optional .dossier.Comment deprecation = 9;</code>
   */
  boolean hasDeprecation();
  /**
   * <code>optional .dossier.Comment deprecation = 9;</code>
   */
  com.github.jsdossier.proto.Comment getDeprecation();
  /**
   * <code>optional .dossier.Comment deprecation = 9;</code>
   */
  com.github.jsdossier.proto.CommentOrBuilder getDeprecationOrBuilder();

  /**
   * <code>repeated .dossier.JsType.TypeDef type_def = 10;</code>
   */
  java.util.List<com.github.jsdossier.proto.JsType.TypeDef> 
      getTypeDefList();
  /**
   * <code>repeated .dossier.JsType.TypeDef type_def = 10;</code>
   */
  com.github.jsdossier.proto.JsType.TypeDef getTypeDef(int index);
  /**
   * <code>repeated .dossier.JsType.TypeDef type_def = 10;</code>
   */
  int getTypeDefCount();
  /**
   * <code>repeated .dossier.JsType.TypeDef type_def = 10;</code>
   */
  java.util.List<? extends com.github.jsdossier.proto.JsType.TypeDefOrBuilder> 
      getTypeDefOrBuilderList();
  /**
   * <code>repeated .dossier.JsType.TypeDef type_def = 10;</code>
   */
  com.github.jsdossier.proto.JsType.TypeDefOrBuilder getTypeDefOrBuilder(
      int index);

  /**
   * <code>optional .dossier.Enumeration enumeration = 11;</code>
   */
  boolean hasEnumeration();
  /**
   * <code>optional .dossier.Enumeration enumeration = 11;</code>
   */
  com.github.jsdossier.proto.Enumeration getEnumeration();
  /**
   * <code>optional .dossier.Enumeration enumeration = 11;</code>
   */
  com.github.jsdossier.proto.EnumerationOrBuilder getEnumerationOrBuilder();

  /**
   * <code>repeated .dossier.Function static_function = 12;</code>
   */
  java.util.List<com.github.jsdossier.proto.Function> 
      getStaticFunctionList();
  /**
   * <code>repeated .dossier.Function static_function = 12;</code>
   */
  com.github.jsdossier.proto.Function getStaticFunction(int index);
  /**
   * <code>repeated .dossier.Function static_function = 12;</code>
   */
  int getStaticFunctionCount();
  /**
   * <code>repeated .dossier.Function static_function = 12;</code>
   */
  java.util.List<? extends com.github.jsdossier.proto.FunctionOrBuilder> 
      getStaticFunctionOrBuilderList();
  /**
   * <code>repeated .dossier.Function static_function = 12;</code>
   */
  com.github.jsdossier.proto.FunctionOrBuilder getStaticFunctionOrBuilder(
      int index);

  /**
   * <code>repeated .dossier.Property static_property = 13;</code>
   */
  java.util.List<com.github.jsdossier.proto.Property> 
      getStaticPropertyList();
  /**
   * <code>repeated .dossier.Property static_property = 13;</code>
   */
  com.github.jsdossier.proto.Property getStaticProperty(int index);
  /**
   * <code>repeated .dossier.Property static_property = 13;</code>
   */
  int getStaticPropertyCount();
  /**
   * <code>repeated .dossier.Property static_property = 13;</code>
   */
  java.util.List<? extends com.github.jsdossier.proto.PropertyOrBuilder> 
      getStaticPropertyOrBuilderList();
  /**
   * <code>repeated .dossier.Property static_property = 13;</code>
   */
  com.github.jsdossier.proto.PropertyOrBuilder getStaticPropertyOrBuilder(
      int index);

  /**
   * <code>optional .dossier.Function main_function = 14;</code>
   *
   * <pre>
   * Describes the main function for this type. This is typically a class
   * constructor, but may also be the main function for a namespace (which
   * all other properties are hung off of).
   * </pre>
   */
  boolean hasMainFunction();
  /**
   * <code>optional .dossier.Function main_function = 14;</code>
   *
   * <pre>
   * Describes the main function for this type. This is typically a class
   * constructor, but may also be the main function for a namespace (which
   * all other properties are hung off of).
   * </pre>
   */
  com.github.jsdossier.proto.Function getMainFunction();
  /**
   * <code>optional .dossier.Function main_function = 14;</code>
   *
   * <pre>
   * Describes the main function for this type. This is typically a class
   * constructor, but may also be the main function for a namespace (which
   * all other properties are hung off of).
   * </pre>
   */
  com.github.jsdossier.proto.FunctionOrBuilder getMainFunctionOrBuilder();

  /**
   * <code>repeated .dossier.Function method = 15;</code>
   *
   * <pre>
   * Instance methods for this type.
   * </pre>
   */
  java.util.List<com.github.jsdossier.proto.Function> 
      getMethodList();
  /**
   * <code>repeated .dossier.Function method = 15;</code>
   *
   * <pre>
   * Instance methods for this type.
   * </pre>
   */
  com.github.jsdossier.proto.Function getMethod(int index);
  /**
   * <code>repeated .dossier.Function method = 15;</code>
   *
   * <pre>
   * Instance methods for this type.
   * </pre>
   */
  int getMethodCount();
  /**
   * <code>repeated .dossier.Function method = 15;</code>
   *
   * <pre>
   * Instance methods for this type.
   * </pre>
   */
  java.util.List<? extends com.github.jsdossier.proto.FunctionOrBuilder> 
      getMethodOrBuilderList();
  /**
   * <code>repeated .dossier.Function method = 15;</code>
   *
   * <pre>
   * Instance methods for this type.
   * </pre>
   */
  com.github.jsdossier.proto.FunctionOrBuilder getMethodOrBuilder(
      int index);

  /**
   * <code>repeated .dossier.Property field = 16;</code>
   *
   * <pre>
   * Instance properties for this type.
   * </pre>
   */
  java.util.List<com.github.jsdossier.proto.Property> 
      getFieldList();
  /**
   * <code>repeated .dossier.Property field = 16;</code>
   *
   * <pre>
   * Instance properties for this type.
   * </pre>
   */
  com.github.jsdossier.proto.Property getField(int index);
  /**
   * <code>repeated .dossier.Property field = 16;</code>
   *
   * <pre>
   * Instance properties for this type.
   * </pre>
   */
  int getFieldCount();
  /**
   * <code>repeated .dossier.Property field = 16;</code>
   *
   * <pre>
   * Instance properties for this type.
   * </pre>
   */
  java.util.List<? extends com.github.jsdossier.proto.PropertyOrBuilder> 
      getFieldOrBuilderList();
  /**
   * <code>repeated .dossier.Property field = 16;</code>
   *
   * <pre>
   * Instance properties for this type.
   * </pre>
   */
  com.github.jsdossier.proto.PropertyOrBuilder getFieldOrBuilder(
      int index);

  /**
   * <code>repeated .dossier.expression.NamedType extended_type = 18;</code>
   *
   * <pre>
   * List of inherited types for a class. This should be empty if |is_class|
   * is false. Classes should be listed in order, with the root type first.
   * </pre>
   */
  java.util.List<com.github.jsdossier.proto.NamedType> 
      getExtendedTypeList();
  /**
   * <code>repeated .dossier.expression.NamedType extended_type = 18;</code>
   *
   * <pre>
   * List of inherited types for a class. This should be empty if |is_class|
   * is false. Classes should be listed in order, with the root type first.
   * </pre>
   */
  com.github.jsdossier.proto.NamedType getExtendedType(int index);
  /**
   * <code>repeated .dossier.expression.NamedType extended_type = 18;</code>
   *
   * <pre>
   * List of inherited types for a class. This should be empty if |is_class|
   * is false. Classes should be listed in order, with the root type first.
   * </pre>
   */
  int getExtendedTypeCount();
  /**
   * <code>repeated .dossier.expression.NamedType extended_type = 18;</code>
   *
   * <pre>
   * List of inherited types for a class. This should be empty if |is_class|
   * is false. Classes should be listed in order, with the root type first.
   * </pre>
   */
  java.util.List<? extends com.github.jsdossier.proto.NamedTypeOrBuilder> 
      getExtendedTypeOrBuilderList();
  /**
   * <code>repeated .dossier.expression.NamedType extended_type = 18;</code>
   *
   * <pre>
   * List of inherited types for a class. This should be empty if |is_class|
   * is false. Classes should be listed in order, with the root type first.
   * </pre>
   */
  com.github.jsdossier.proto.NamedTypeOrBuilder getExtendedTypeOrBuilder(
      int index);

  /**
   * <code>repeated .dossier.expression.NamedType implemented_type = 19;</code>
   *
   * <pre>
   * The interfaces implemented by a class/extended by an interface. This
   * list should be empty if |is_class| and |is_interface| are false.
   * </pre>
   */
  java.util.List<com.github.jsdossier.proto.NamedType> 
      getImplementedTypeList();
  /**
   * <code>repeated .dossier.expression.NamedType implemented_type = 19;</code>
   *
   * <pre>
   * The interfaces implemented by a class/extended by an interface. This
   * list should be empty if |is_class| and |is_interface| are false.
   * </pre>
   */
  com.github.jsdossier.proto.NamedType getImplementedType(int index);
  /**
   * <code>repeated .dossier.expression.NamedType implemented_type = 19;</code>
   *
   * <pre>
   * The interfaces implemented by a class/extended by an interface. This
   * list should be empty if |is_class| and |is_interface| are false.
   * </pre>
   */
  int getImplementedTypeCount();
  /**
   * <code>repeated .dossier.expression.NamedType implemented_type = 19;</code>
   *
   * <pre>
   * The interfaces implemented by a class/extended by an interface. This
   * list should be empty if |is_class| and |is_interface| are false.
   * </pre>
   */
  java.util.List<? extends com.github.jsdossier.proto.NamedTypeOrBuilder> 
      getImplementedTypeOrBuilderList();
  /**
   * <code>repeated .dossier.expression.NamedType implemented_type = 19;</code>
   *
   * <pre>
   * The interfaces implemented by a class/extended by an interface. This
   * list should be empty if |is_class| and |is_interface| are false.
   * </pre>
   */
  com.github.jsdossier.proto.NamedTypeOrBuilder getImplementedTypeOrBuilder(
      int index);

  /**
   * <code>repeated .dossier.expression.NamedType subtype = 26;</code>
   *
   * <pre>
   * Known subtypes of this type. If |is_class| is true, this will contain the
   * know direct subclasses. If |is_interface| is true, this will contain the
   * known subinterfaces.
   * </pre>
   */
  java.util.List<com.github.jsdossier.proto.NamedType> 
      getSubtypeList();
  /**
   * <code>repeated .dossier.expression.NamedType subtype = 26;</code>
   *
   * <pre>
   * Known subtypes of this type. If |is_class| is true, this will contain the
   * know direct subclasses. If |is_interface| is true, this will contain the
   * known subinterfaces.
   * </pre>
   */
  com.github.jsdossier.proto.NamedType getSubtype(int index);
  /**
   * <code>repeated .dossier.expression.NamedType subtype = 26;</code>
   *
   * <pre>
   * Known subtypes of this type. If |is_class| is true, this will contain the
   * know direct subclasses. If |is_interface| is true, this will contain the
   * known subinterfaces.
   * </pre>
   */
  int getSubtypeCount();
  /**
   * <code>repeated .dossier.expression.NamedType subtype = 26;</code>
   *
   * <pre>
   * Known subtypes of this type. If |is_class| is true, this will contain the
   * know direct subclasses. If |is_interface| is true, this will contain the
   * known subinterfaces.
   * </pre>
   */
  java.util.List<? extends com.github.jsdossier.proto.NamedTypeOrBuilder> 
      getSubtypeOrBuilderList();
  /**
   * <code>repeated .dossier.expression.NamedType subtype = 26;</code>
   *
   * <pre>
   * Known subtypes of this type. If |is_class| is true, this will contain the
   * know direct subclasses. If |is_interface| is true, this will contain the
   * known subinterfaces.
   * </pre>
   */
  com.github.jsdossier.proto.NamedTypeOrBuilder getSubtypeOrBuilder(
      int index);

  /**
   * <code>repeated .dossier.expression.NamedType implementation = 27;</code>
   *
   * <pre>
   * Known implementations of this type. Will be empy if |is_interface| is false.
   * </pre>
   */
  java.util.List<com.github.jsdossier.proto.NamedType> 
      getImplementationList();
  /**
   * <code>repeated .dossier.expression.NamedType implementation = 27;</code>
   *
   * <pre>
   * Known implementations of this type. Will be empy if |is_interface| is false.
   * </pre>
   */
  com.github.jsdossier.proto.NamedType getImplementation(int index);
  /**
   * <code>repeated .dossier.expression.NamedType implementation = 27;</code>
   *
   * <pre>
   * Known implementations of this type. Will be empy if |is_interface| is false.
   * </pre>
   */
  int getImplementationCount();
  /**
   * <code>repeated .dossier.expression.NamedType implementation = 27;</code>
   *
   * <pre>
   * Known implementations of this type. Will be empy if |is_interface| is false.
   * </pre>
   */
  java.util.List<? extends com.github.jsdossier.proto.NamedTypeOrBuilder> 
      getImplementationOrBuilderList();
  /**
   * <code>repeated .dossier.expression.NamedType implementation = 27;</code>
   *
   * <pre>
   * Known implementations of this type. Will be empy if |is_interface| is false.
   * </pre>
   */
  com.github.jsdossier.proto.NamedTypeOrBuilder getImplementationOrBuilder(
      int index);

  /**
   * <code>repeated .dossier.Property compiler_constant = 20;</code>
   */
  java.util.List<com.github.jsdossier.proto.Property> 
      getCompilerConstantList();
  /**
   * <code>repeated .dossier.Property compiler_constant = 20;</code>
   */
  com.github.jsdossier.proto.Property getCompilerConstant(int index);
  /**
   * <code>repeated .dossier.Property compiler_constant = 20;</code>
   */
  int getCompilerConstantCount();
  /**
   * <code>repeated .dossier.Property compiler_constant = 20;</code>
   */
  java.util.List<? extends com.github.jsdossier.proto.PropertyOrBuilder> 
      getCompilerConstantOrBuilderList();
  /**
   * <code>repeated .dossier.Property compiler_constant = 20;</code>
   */
  com.github.jsdossier.proto.PropertyOrBuilder getCompilerConstantOrBuilder(
      int index);

  /**
   * <code>optional .dossier.JsType.ParentLink parent = 21;</code>
   */
  boolean hasParent();
  /**
   * <code>optional .dossier.JsType.ParentLink parent = 21;</code>
   */
  com.github.jsdossier.proto.JsType.ParentLink getParent();
  /**
   * <code>optional .dossier.JsType.ParentLink parent = 21;</code>
   */
  com.github.jsdossier.proto.JsType.ParentLinkOrBuilder getParentOrBuilder();

  /**
   * <code>optional .dossier.expression.NamedType aliased_type = 22;</code>
   *
   * <pre>
   * Link to another type that this type is an alias of.
   * </pre>
   */
  boolean hasAliasedType();
  /**
   * <code>optional .dossier.expression.NamedType aliased_type = 22;</code>
   *
   * <pre>
   * Link to another type that this type is an alias of.
   * </pre>
   */
  com.github.jsdossier.proto.NamedType getAliasedType();
  /**
   * <code>optional .dossier.expression.NamedType aliased_type = 22;</code>
   *
   * <pre>
   * Link to another type that this type is an alias of.
   * </pre>
   */
  com.github.jsdossier.proto.NamedTypeOrBuilder getAliasedTypeOrBuilder();

  /**
   * <code>repeated .dossier.expression.NamedType known_alias = 28;</code>
   */
  java.util.List<com.github.jsdossier.proto.NamedType> 
      getKnownAliasList();
  /**
   * <code>repeated .dossier.expression.NamedType known_alias = 28;</code>
   */
  com.github.jsdossier.proto.NamedType getKnownAlias(int index);
  /**
   * <code>repeated .dossier.expression.NamedType known_alias = 28;</code>
   */
  int getKnownAliasCount();
  /**
   * <code>repeated .dossier.expression.NamedType known_alias = 28;</code>
   */
  java.util.List<? extends com.github.jsdossier.proto.NamedTypeOrBuilder> 
      getKnownAliasOrBuilderList();
  /**
   * <code>repeated .dossier.expression.NamedType known_alias = 28;</code>
   */
  com.github.jsdossier.proto.NamedTypeOrBuilder getKnownAliasOrBuilder(
      int index);

  /**
   * <code>optional string filename = 23;</code>
   *
   * <pre>
   * Base name for the rendered file for this type.
   * </pre>
   */
  java.lang.String getFilename();
  /**
   * <code>optional string filename = 23;</code>
   *
   * <pre>
   * Base name for the rendered file for this type.
   * </pre>
   */
  com.google.protobuf.ByteString
      getFilenameBytes();

  /**
   * <code>optional string qualified_name = 24;</code>
   *
   * <pre>
   * Qualified display name for this type.
   * </pre>
   */
  java.lang.String getQualifiedName();
  /**
   * <code>optional string qualified_name = 24;</code>
   *
   * <pre>
   * Qualified display name for this type.
   * </pre>
   */
  com.google.protobuf.ByteString
      getQualifiedNameBytes();

  /**
   * <code>repeated .dossier.Property reexported_module = 25;</code>
   *
   * <pre>
   * Re-exported modules.
   * </pre>
   */
  java.util.List<com.github.jsdossier.proto.Property> 
      getReexportedModuleList();
  /**
   * <code>repeated .dossier.Property reexported_module = 25;</code>
   *
   * <pre>
   * Re-exported modules.
   * </pre>
   */
  com.github.jsdossier.proto.Property getReexportedModule(int index);
  /**
   * <code>repeated .dossier.Property reexported_module = 25;</code>
   *
   * <pre>
   * Re-exported modules.
   * </pre>
   */
  int getReexportedModuleCount();
  /**
   * <code>repeated .dossier.Property reexported_module = 25;</code>
   *
   * <pre>
   * Re-exported modules.
   * </pre>
   */
  java.util.List<? extends com.github.jsdossier.proto.PropertyOrBuilder> 
      getReexportedModuleOrBuilderList();
  /**
   * <code>repeated .dossier.Property reexported_module = 25;</code>
   *
   * <pre>
   * Re-exported modules.
   * </pre>
   */
  com.github.jsdossier.proto.PropertyOrBuilder getReexportedModuleOrBuilder(
      int index);
}
