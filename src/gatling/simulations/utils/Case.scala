package utils

object Case {
  object Gender extends Enumeration {
    val Male = Value("male")
    val Female = Value("female")
  }

  object Checkbox extends Enumeration {
    val Checked = Value("checked")
    val Unchecked = Value("")
  }

  object YesOrNo extends Enumeration {
    val Yes = Value("Yes")
    val No = Value("No")
  }
}