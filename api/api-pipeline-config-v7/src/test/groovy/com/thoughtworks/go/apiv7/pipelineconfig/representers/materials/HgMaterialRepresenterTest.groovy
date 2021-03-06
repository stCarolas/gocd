/*
 * Copyright 2019 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.go.apiv7.pipelineconfig.representers.materials

import com.thoughtworks.go.api.util.GsonTransformer
import com.thoughtworks.go.apiv7.admin.pipelineconfig.representers.materials.MaterialsRepresenter
import com.thoughtworks.go.apiv7.admin.shared.representers.stages.ConfigHelperOptions
import com.thoughtworks.go.config.BasicCruiseConfig
import com.thoughtworks.go.config.CaseInsensitiveString
import com.thoughtworks.go.config.PipelineConfig
import com.thoughtworks.go.config.PipelineConfigSaveValidationContext
import com.thoughtworks.go.config.materials.MaterialConfigs
import com.thoughtworks.go.config.materials.PasswordDeserializer
import com.thoughtworks.go.config.materials.mercurial.HgMaterialConfig
import com.thoughtworks.go.helper.MaterialConfigsMother
import com.thoughtworks.go.util.command.HgUrlArgument
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

import static org.assertj.core.api.Assertions.assertThat
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull
import static org.mockito.Mockito.mock

class HgMaterialRepresenterTest implements MaterialRepresenterTrait {

  static def existingMaterial() {
    return MaterialConfigsMother.hgMaterialConfigFull()
  }

  def getOptions() {
    return new ConfigHelperOptions(mock(BasicCruiseConfig.class), mock(PasswordDeserializer.class))
  }

  def existingMaterialWithErrors() {
    def hgConfig = new HgMaterialConfig(new HgUrlArgument(''), true, null, false, '/dest/', new CaseInsensitiveString('!nV@l!d'))
    def materialConfigs = new MaterialConfigs(hgConfig);
    materialConfigs.validateTree(PipelineConfigSaveValidationContext.forChain(true, "group", new BasicCruiseConfig(), new PipelineConfig()))
    return materialConfigs.get(0)
  }


  @Nested
  class Credentials {
    @Test
    void "should deserialize material with credentials in URL"() {
      def jsonReader = GsonTransformer.instance.jsonReaderFrom([
        type      : 'hg',
        attributes:
          [
            url   : "http://user:password@funk.com/blank",
            branch: "master"
          ]
      ])

      def deserializedObject = MaterialsRepresenter.fromJSON(jsonReader, getOptions())
      def expected = new HgMaterialConfig("http://user:password@funk.com/blank", null)

      assertEquals(expected.isAutoUpdate(), deserializedObject.isAutoUpdate())
      assertNull(deserializedObject.getName())
      assertEquals(expected, deserializedObject)
    }

    @Test
    void "should deserialize material with credentials in attributes"() {
      def jsonReader = GsonTransformer.instance.jsonReaderFrom([
        type      : 'hg',
        attributes:
          [
            url     : "http://funk.com/blank",
            branch  : "master",
            username: "user",
            password: "password"
          ]
      ])

      def deserializedObject = MaterialsRepresenter.fromJSON(jsonReader, getOptions())
      def expected = new HgMaterialConfig("http://user:password@funk.com/blank", null)

      assertEquals(expected.isAutoUpdate(), deserializedObject.isAutoUpdate())
      assertNull(deserializedObject.getName())
      assertEquals(expected, deserializedObject)
    }

    @Test
    void "should add errors on deserialize where material has ambiguous credentials"() {
      def jsonReader = GsonTransformer.instance.jsonReaderFrom([
        type      : 'hg',
        attributes:
          [
            url     : "http://user1:password1@funk.com/blank",
            branch  : "master",
            username: "user2",
            password: "password2"
          ]
      ])

      def deserializedObject = MaterialsRepresenter.fromJSON(jsonReader, getOptions())

      assertThat(deserializedObject.errors().get("url"))
        .contains("You may only specify credentials in `url` or attributes, not both!".toString())
      assertThat(deserializedObject.errors().get("username"))
        .contains("You may only specify credentials in `url` or attributes, not both!".toString())
      assertThat(deserializedObject.errors().get("password"))
        .contains("You may only specify credentials in `url` or attributes, not both!".toString())
      assertThat(deserializedObject.errors().get("encrypted_password")).isNull()
    }
  }

  def materialHash =
    [
      type      : 'hg',
      attributes: [
        url          : "http://user:pass@domain/path##branch",
        destination  : "dest-folder",
        filter       : [
          ignore: ['**/*.html', '**/foobar/']
        ],
        invert_filter: false,
        name         : "hg-material",
        auto_update  : true
      ]
    ]

  def expectedMaterialHashWithErrors =
    [
      type      : "hg",
      attributes: [
        url          : "",
        destination  : "/dest/",
        filter       : null,
        invert_filter: false,
        name         : "!nV@l!d",
        auto_update  : true
      ],
      errors    : [
        name       : ["Invalid material name '!nV@l!d'. This must be alphanumeric and can contain underscores and periods (however, it cannot start with a period). The maximum allowed length is 255 characters."],
        destination: ["Dest folder '/dest/' is not valid. It must be a sub-directory of the working folder."],
        url        : ["URL cannot be blank"]
      ]
    ]
}
