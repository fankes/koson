package io.github.ncomet.koson

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows

@TestInstance(PER_CLASS)
class KosonTest : WithAssertions {

    @Test
    fun `empty object`() {
        assertThat(obj { }.toString()).isEqualTo("{}")
    }

    @Test
    fun `empty array`() {
        assertThat(array.toString()).isEqualTo("[]")
    }

    @Test
    fun `array containing this as a value should render`() {
        array[this]
    }

    object SimpleObject {
        override fun toString(): String = this.javaClass.simpleName
    }

    object ContainsDoubleQuotes {
        override fun toString(): String = "\"unfor\"tunate\""
    }

    @Test
    fun `object with all possible types of value`() {
        assertThat(obj {
            "string" to "value"
            "double" to 7.6
            "float" to 3.2f
            "long" to 34L
            "int" to 9
            "char" to 'e'
            "short" to 12.toShort()
            "byte" to 0x32
            "boolean" to false
            "object" to obj { }
            "emptyArray" to array
            "array" to array["test"]
            "null" to null
            "any" to SimpleObject
            "custom" to ContainsDoubleQuotes
        }.toString()).isEqualTo("{\"string\":\"value\",\"double\":7.6,\"float\":3.2,\"long\":34,\"int\":9,\"char\":\"e\",\"short\":12,\"byte\":50,\"boolean\":false,\"object\":{},\"emptyArray\":[],\"array\":[\"test\"],\"null\":null,\"any\":\"SimpleObject\",\"custom\":\"\\\"unfor\\\"tunate\\\"\"}")
    }

    @Test
    internal fun `array with all possible types of value`() {
        assertThat(
            array[
                    "value",
                    7.6,
                    3.2f,
                    34L,
                    9,
                    'e',
                    12.toShort(),
                    0x32,
                    false,
                    obj { },
                    array,
                    array["test"],
                    null,
                    SimpleObject,
                    ContainsDoubleQuotes
            ].toString()
        ).isEqualTo("[\"value\",7.6,3.2,34,9,\"e\",12,50,false,{},[],[\"test\"],null,\"SimpleObject\",\"\\\"unfor\\\"tunate\\\"\"]")
    }

    @Nested
    inner class ContainingCases : WithAssertions {
        @Test
        fun `object containing array`() {
            assertThat(obj { "array" to array }.toString()).isEqualTo("{\"array\":[]}")
        }

        @Test
        fun `array containing object`() {
            assertThat(array[obj { }].toString()).isEqualTo("[{}]")
        }

        @Test
        fun `object containing object`() {
            assertThat(obj { "object" to obj { } }.toString()).isEqualTo("{\"object\":{}}")
        }

        @Test
        fun `array containing array`() {
            assertThat(array[array].toString()).isEqualTo("[[]]")
        }

        @Test
        @Suppress("UNUSED_EXPRESSION")
        fun `object not containing a to() should do nothing`() {
            assertThat(obj { "content" }.toString()).isEqualTo("{}")
        }
    }

    @Nested
    inner class MoreComplexCases : WithAssertions {
        @Test
        fun `constructing a bit more complex object`() {
            val obj = obj {
                "key" to 3.4
                "anotherKey" to array["test", "test2", 1, 2.433, true]
                "nullsAreAllowedToo" to null
                "array" to array[
                        obj {
                            "double" to 33.4
                            "float" to 345f
                            "long" to 21L
                            "int" to 42
                            "char" to 'a'
                            "byte" to 0xAA
                            "otherArray" to array
                            "simpleObject" to SimpleObject
                        }
                ]
            }

            assertThat("$obj")
                .isEqualTo("{\"key\":3.4,\"anotherKey\":[\"test\",\"test2\",1,2.433,true],\"nullsAreAllowedToo\":null,\"array\":[{\"double\":33.4,\"float\":345.0,\"long\":21,\"int\":42,\"char\":\"a\",\"byte\":170,\"otherArray\":[],\"simpleObject\":\"SimpleObject\"}]}")
        }

        @Test
        fun `contructing a bit more complex array`() {
            val array = array["koson", 33.4, 345f, 21L, 42, 'a', 0x21,
                    obj {
                        "aKey" to "value"
                        "insideArray" to array
                        "otherArray" to array["element", ContainsDoubleQuotes, obj { }]
                    }
            ]

            assertThat("$array")
                .isEqualTo("[\"koson\",33.4,345.0,21,42,\"a\",33,{\"aKey\":\"value\",\"insideArray\":[],\"otherArray\":[\"element\",\"\\\"unfor\\\"tunate\\\"\",{}]}]")
        }

        @Test
        fun `testing an object inlined`() {
            val obj =
                obj { "key" to 3.4; "anotherKey" to array["test", "test2", 1, 2.433, true]; "nullsAreAllowedToo" to null }

            assertThat("$obj")
                .isEqualTo("{\"key\":3.4,\"anotherKey\":[\"test\",\"test2\",1,2.433,true],\"nullsAreAllowedToo\":null}")
        }
    }

    @Nested
    inner class ExceptionCases : WithAssertions {

        @Test
        fun `object construction must throw KosonException when duplicate key`() {
            val message = assertThrows<KosonException> {
                obj {
                    "key" to "myVal"
                    "key" to 1.65
                }
            }.message

            assertThat(message).isEqualTo("key <key> of (key to 1.65) is already defined for json object")
        }

    }

}
