# Akka Http (bind & bindAndHandle) 

Simple akka http server runs on `8585` (bindAndHandle) and `8686` (bind) ports. I was expecting `bindAndHandle` perform with comparitive `bind`.

|                                        |Requests          |Total  |  OK   |  KO   |KO%|Req.Sec|Min|50th |75th |95th |99th |Max    |Mean |StdDiv|
|----------------------------------------|------------------|-------|-------|-------|---|-------|---|-----|-----|-----|-----|-------|-----|------|
|BindAndHandle                           |Global Information|10000  |6617   |3383   |34%|166.667|0  |163  |10005|27264|36294|51389  |5903 |9271  |
|Bind                                    |Global Information|10000  |7468   |2532   |25%|333.333|0  |560  |5033 |10671|17003|24333  |3036 |4369  |
|BindAndHandle                           |Ping              |5000   |2244   |2756   |55%|83.333 |3  |8520 |16880|32187|36568|51389  |10297|10537 |
|Bind                                    |Ping              |5000   |2961   |2039   |41%|166.667|2  |2440 |10008|12871|17618|24333  |4805 |4817  |
|BindAndHandle                           |IsPrime           |5000   |4373   |627    |13%|83.333 |0  |5    |120  |10283|24268|49177  |1508 |4718  |
|Bind                                    |IsPrime           |5000   |4507   |493    |10%|166.667|0  |10   |507  |9577 |12927|20949  |1266 |2951  |