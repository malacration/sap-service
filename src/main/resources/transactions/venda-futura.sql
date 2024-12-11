IF :object_type = '17' then
	IF( EXISTS(
		SELECT
			1
		FROM
			ORDR
		WHERE
			"UserSign2" != 162
			AND "DocEntry" = :list_of_cols_val_tab_del
			AND
			(
			"U_pedido_update" = '1'
			OR
			1 = (SELECT
					TOP 1 "U_pedido_update"
				FROM
					ADOC
				WHERE
					"ObjType" = 17
					AND "DocEntry" = :list_of_cols_val_tab_del
				GROUP BY "U_pedido_update"
				ORDER BY MAX("LogInstanc") DESC))
			)) THEN
		error := '666';
    	error_message := 'O item esta bloqueado para atualização de imposto desonerado';
	END if;
END IF;