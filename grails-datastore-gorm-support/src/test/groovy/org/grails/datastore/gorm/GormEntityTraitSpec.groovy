package org.grails.datastore.gorm

import grails.artefact.Artefact
import grails.persistence.Entity
import org.grails.datastore.mapping.dirty.checking.DirtyCheckable
import spock.lang.Specification

/*
 * Copyright 2014 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author graemerocher
 */
class GormEntityTraitSpec extends Specification {

    void "Test dynamic parse"(){
        when:
        def cls = new GroovyClassLoader().parseClass('''
import grails.persistence.*

@Entity
class Book {

    String title

    Author author
}

@Entity
class Author {

    String name
    // here to test properties with only a single letter
    static belongsTo = [p:Publisher]
}

@Entity
class Publisher {
    String name
}
''')
        def instance = cls.newInstance()

        then:
        cls.transients.contains('authorId')
        GormEntity.isAssignableFrom(cls)
        GormValidateable.isAssignableFrom(cls)
        DirtyCheckable.isAssignableFrom(cls)
        cls.getAnnotation(grails.gorm.Entity)
        instance.hasProperty('authorId')
    }

    void "Test dynamic parse 2"(){
        when:
        def cls = new GroovyClassLoader().parseClass('''
import grails.persistence.*

@Entity
class Group {
    Long id
    String name
    static hasMany = [members:Member]
    Collection members
}

@Entity
class Member   {
    Long id
    String name
    String externalId
}

@Entity
class SubMember extends Member {
    String extraName
}

''')
        def instance = cls.newInstance()
        then:
        instance.respondsTo('addToMembers')
        GormEntity.isAssignableFrom(cls)
        GormValidateable.isAssignableFrom(cls)
        DirtyCheckable.isAssignableFrom(cls)
        cls.getAnnotation(grails.gorm.Entity)
    }
    void "test that a class marked with @Artefact('Domain') is enhanced with GormEntityTraitSpec"() {
        expect:
        GormEntity.isAssignableFrom QueryMethodArtefactDomain
    }

    void "test that a class marked with @Entity is enhanced with GormEntityTraitSpec"() {
        expect:
        GormEntity.isAssignableFrom QueryMethodEntityDomain
    }

    void 'test that generic return values are respected'() {
        when:
        def method = QueryMethodArtefactDomain.methods.find { method ->
            def rt = method.getParameterTypes()
            rt && rt[0] == Closure && method.name == 'find'
        }

        then:
        method.returnType == QueryMethodArtefactDomain
    }
}

@Artefact('Domain')
class QueryMethodArtefactDomain {
    String name
}

@Entity
class QueryMethodEntityDomain {
    String name
}
