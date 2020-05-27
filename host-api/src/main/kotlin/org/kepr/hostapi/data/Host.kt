package org.kepr.hostapi.data

import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Entity(name="host")
data class Host(@Column(unique=true) @NotBlank var address: String, @Column(unique=true) @NotBlank var name: String, @ManyToOne(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
var alias: Alias? = null) {

    @Id @GeneratedValue val id: Long? = null

}