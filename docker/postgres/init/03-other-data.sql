INSERT INTO usuario (ativo, created_at, updated_at, email, nome, login, ecm_user_id) VALUES(true, now(), now(), 'jonathas.pdcase@emap.ma.gov.br', 'Jonathas Pimenta', 'jonathas.pdcase', 7141);
INSERT INTO usuario (ativo, created_at, updated_at, email, nome, login, ecm_user_id) VALUES(true, now(), now(), 'yandson.pdcase@emap.ma.gov.br', 'Yandson Saraiva', 'yandson.pdcase', 6041);
INSERT INTO perfil (nome) values ('ADMIN'), ('AUDITOR'), ('GESTOR'), ('AUDITADO') , ('DEFAULT');
INSERT INTO usuario_perfil (usuario_id, perfil_id) SELECT u.id, p.id FROM usuario u CROSS JOIN perfil p WHERE u.login = 'jonathas.pdcase' AND p.nome IN ('ADMIN', 'DEFAULT');
INSERT INTO usuario_perfil (usuario_id, perfil_id) SELECT u.id, p.id FROM usuario u CROSS JOIN perfil p WHERE u.login = 'yandson.pdcase' AND p.nome IN ('DEFAULT');

/*ALTER TABLE public.campanha ADD ecm_cod_audit bigint null;
ALTER TABLE public.auditoria ADD ecm_processo_id bigint null;
ALTER TABLE public.usuario ADD ecm_user_id bigint null;
ALTER TABLE public.campanha ADD ecm_last_sync timestamp(6) with time zone null;
*/